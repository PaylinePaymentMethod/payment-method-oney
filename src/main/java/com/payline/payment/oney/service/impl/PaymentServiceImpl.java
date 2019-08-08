package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.customer.PurchaseHistory;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.bean.request.OneyPaymentRequest;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.payment.oney.bean.response.OneySuccessPaymentResponse;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.RequestContext;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.bean.response.OneySuccessPaymentResponse.paymentSuccessResponseFromJson;
import static com.payline.payment.oney.bean.response.PaymentErrorResponse.paymentErrorResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.OneyErrorHandler.handleOneyFailureResponse;

public class PaymentServiceImpl implements PaymentService {

    private OneyHttpClient httpClient;
    private BeanAssembleService beanAssembleService = BeanAssemblerServiceImpl.getInstance();
    private static final Logger LOGGER = LogManager.getLogger(PaymentServiceImpl.class);


    public PaymentServiceImpl() {
        this.httpClient = OneyHttpClient.getInstance();
    }

    @Override
    public PaymentResponse paymentRequest(PaymentRequest paymentRequest) {
        try {
            final String merchGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, MERCHANT_GUID_KEY);
            final String merchLanguage = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, LANGUAGE_CODE_KEY);
            final String language = paymentRequest.getLocale().getLanguage();
            final String merchantRequestId = PluginUtils.generateMerchantRequestId(merchGuid);
            final String pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PSP_GUID_KEY);
            final String chiffrementKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PARTNER_CHIFFREMENT_KEY);
            final BusinessTransactionData businessTransaction = beanAssembleService.assembleBuisnessTransactionData(paymentRequest);
            final PaymentData paymentData = beanAssembleService.assemblePaymentData(paymentRequest, businessTransaction);
            final NavigationData navigationData = beanAssembleService.assembleNavigationData(paymentRequest);
            final Customer customer = beanAssembleService.assembleCustomer(paymentRequest);
            final Purchase purchase = beanAssembleService.assemblePurchase(paymentRequest);
            final String merchantContext = PluginUtils.createMerchantContext(paymentRequest);
            final PurchaseHistory purchaseHistory = beanAssembleService.assemblePurchaseHistory(paymentRequest);

            final OneyPaymentRequest oneyRequest = OneyPaymentRequest.Builder.aOneyPaymentRequest()
                    .withLanguageCode(language)
                    .withMerchantRequestId(merchantRequestId)
                    .withPspGuid(pspGuid)
                    .withMerchantGuid(merchGuid)
                    .withNavigation(navigationData)
                    .withPaymentdata(paymentData)
                    .withCustomer(customer)
                    .withPurchase(purchase)
                    .withPurchaseHistory(purchaseHistory)
                    .withMerchantLanguageCode(merchLanguage)
                    .withEncryptKey(chiffrementKey)
                    .withMerchantContext(paymentRequest.getSoftDescriptor())
                    .withPspContext(paymentRequest.getTransactionId())
                    .withCallParameters(PluginUtils.getParametersMap(paymentRequest))
                    .withMerchantContext(merchantContext)// adding data to merchantContext field to get it later on the notification
                    .build();

            final StringResponse oneyResponse = httpClient.initiatePayment(oneyRequest, paymentRequest.getEnvironment().isSandbox());

            if (oneyResponse == null) {
                LOGGER.debug("InitiateSignatureResponse StringResponse is null !");
                LOGGER.error("Payment is null");
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.PARTNER_UNKNOWN_ERROR,
                        oneyRequest.getPurchase().getExternalReference(),
                        "Empty partner response");

            }
            //Cas ou une erreur est renvoyée au moment du paiement
            if (oneyResponse.getCode() != HTTP_OK) {
                OneyFailureResponse failureResponse = new OneyFailureResponse(oneyResponse.getCode(), oneyResponse.getMessage(), oneyResponse.getContent(), paymentErrorResponseFromJson(oneyResponse.getContent()));

                LOGGER.error("Payment failed {} ", failureResponse.getContent());

                return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                        .withFailureCause(handleOneyFailureResponse(failureResponse))
                        .withErrorCode(failureResponse.toPaylineErrorCode())
                        .build();
            } else {
                //Response OK on recupere url envoyee par Oney
                OneySuccessPaymentResponse successResponse = paymentSuccessResponseFromJson(oneyResponse.getContent(), oneyRequest.getEncryptKey());

                URL redirectURL = successResponse.getReturnedUrlAsUrl();
                PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder responseRedirectURL = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder.aRedirectionRequest()
                        .withUrl(redirectURL);

                PaymentResponseRedirect.RedirectionRequest redirectionRequest = new PaymentResponseRedirect.RedirectionRequest(responseRedirectURL);
                Map<String, String> oneyContext = new HashMap<>();
                //RequestData
                oneyContext.put(OneyConstants.PSP_GUID_KEY, pspGuid);
                oneyContext.put(OneyConstants.MERCHANT_GUID_KEY, merchGuid);
                oneyContext.put(OneyConstants.EXTERNAL_REFERENCE_KEY, OneyConstants.EXTERNAL_REFERENCE_TYPE + OneyConstants.PIPE + purchase.getExternalReference());
                oneyContext.put(OneyConstants.PAYMENT_AMOUNT_KEY, paymentData.getAmount().toString());
                oneyContext.put(OneyConstants.LANGUAGE_CODE_KEY, language);

                RequestContext requestContext = RequestContext.RequestContextBuilder.aRequestContext()
                        .withRequestData(oneyContext)
                        .build();

                return PaymentResponseRedirect.PaymentResponseRedirectBuilder.aPaymentResponseRedirect()
                        .withRedirectionRequest(redirectionRequest)
                        .withPartnerTransactionId(oneyRequest.getPurchase().getExternalReference())
                        .withStatusCode(String.valueOf(oneyResponse.getCode()))
                        .withRequestContext(requestContext)
                        .build();
            }

        } catch (PluginTechnicalException e) {
            return e.toPaymentResponseFailure();
        }

    }

}
