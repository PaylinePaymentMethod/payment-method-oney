package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.bean.request.OneyPaymentRequest;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.payment.oney.bean.response.OneySuccessPaymentResponse;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.exception.InvalidRequestException;
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
import com.payline.pmapi.service.PaymentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.bean.response.OneySuccessPaymentResponse.paymentSuccessResponseFromJson;
import static com.payline.payment.oney.bean.response.PaymentErrorResponse.paymentErrorResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.OneyErrorHandler.getPaymentResponseFailure;
import static com.payline.payment.oney.utils.OneyErrorHandler.handleOneyFailureResponse;
import static com.payline.payment.oney.utils.PluginUtils.generateReference;

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
            final String merchGuid = paymentRequest.getContractConfiguration().getProperty(MERCHANT_GUID_KEY).getValue();
            final String language = paymentRequest.getLocale().getLanguage();
            final String merchantRequestId = PluginUtils.generateMerchantRequestId(merchGuid);
            final String pspGuid = paymentRequest.getPartnerConfiguration().getProperty(PSP_GUID_KEY);
            final String chiffrementKey = paymentRequest.getPartnerConfiguration().getProperty(PARTNER_CHIFFREMENT_KEY);
            final BusinessTransactionData businessTransaction = beanAssembleService.assembleBuisnessTransactionData(paymentRequest);
            final PaymentData paymentData = beanAssembleService.assemblePaymentData(paymentRequest, businessTransaction);
            final NavigationData navigationData = beanAssembleService.assembleNavigationData(paymentRequest);
            final Customer customer = beanAssembleService.assembleCustomer(paymentRequest);
            final Purchase purchase = beanAssembleService.assemblePurchase(paymentRequest);

            final OneyPaymentRequest oneyRequest = OneyPaymentRequest.Builder.aOneyPaymentRequest()
                    .withLanguageCode(language)
                    .withMerchantRequestId(merchantRequestId)
                    .withPspGuid(pspGuid)
                    .withMerchantGuid(merchGuid)
                    .withNavigation(navigationData)
                    .withPaymentdata(paymentData)
                    .withCustomer(customer)
                    .withPurchase(purchase)
                    .withMerchantLanguageCode(language)
                    .withEncryptKey(chiffrementKey)
                    .withMerchantContext(paymentRequest.getSoftDescriptor())
                    .withPspContext(paymentRequest.getTransactionId())
                    .withCallParameters(PluginUtils.getParametersMap(paymentRequest.getPartnerConfiguration(), "BE"))
                    .build();

            final StringResponse oneyResponse = httpClient.initiatePayment(oneyRequest);

            if (oneyResponse == null) {
                LOGGER.debug("InitiateSignatureResponse StringResponse is null !");
                LOGGER.error("Payment is null");
                return OneyErrorHandler.getPaymentResponseFailure(FailureCause.INTERNAL_ERROR, oneyRequest.getPurchase().getExternalReference());

            }
            //Cas ou une erreur est renvoyée au moment du paiement
            if (oneyResponse.getCode() != HTTP_OK) {
                OneyFailureResponse failureResponse = new OneyFailureResponse(oneyResponse.getCode(), oneyResponse.getMessage(), oneyResponse.getContent(), paymentErrorResponseFromJson(oneyResponse.getContent()));

                LOGGER.error("Payment failed {} ", failureResponse.getContent());

                return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                        .withFailureCause(handleOneyFailureResponse(failureResponse))
                        .withErrorCode(failureResponse.getCode().toString())
                        .build();
            } else {
                //Response OK on recupere url envoyee par Oney
                OneySuccessPaymentResponse successResponse = paymentSuccessResponseFromJson(oneyResponse.getContent(), oneyRequest.getEncryptKey());

                URL redirectURL = new URL(successResponse.getReturnedUrl());
                PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder responseRedirectURL = PaymentResponseRedirect.RedirectionRequest.RedirectionRequestBuilder.aRedirectionRequest()
                        .withUrl(redirectURL);

                PaymentResponseRedirect.RedirectionRequest redirectionRequest = new PaymentResponseRedirect.RedirectionRequest(responseRedirectURL);
                Map<String, String> oneyContext = new HashMap<>();
                //RequestData
                oneyContext.put(OneyConstants.PSP_GUID_KEY, oneyRequest.getPspGuid());
                oneyContext.put(OneyConstants.MERCHANT_GUID_KEY, oneyRequest.getMerchantGuid());
                oneyContext.put(OneyConstants.EXTERNAL_REFERENCE_KEY, generateReference(oneyRequest.getPurchase()));
                oneyContext.put(OneyConstants.PAYMENT_AMOUNT_KEY, oneyRequest.getPaymentData().getAmount().toString());
                //Ajout language code ??
                oneyContext.put(OneyConstants.LANGUAGE_CODE_KEY, paymentRequest.getLocale().getLanguage());

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

        } catch (IOException | URISyntaxException | InvalidRequestException | DecryptException e) {
            LOGGER.error("unable init the payment", e);
            return getPaymentResponseFailure(FailureCause.INTERNAL_ERROR);
        }

    }

}
