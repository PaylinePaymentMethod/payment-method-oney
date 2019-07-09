package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.response.OneyNotificationResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.Message;
import com.payline.pmapi.bean.common.OnHoldCause;
import com.payline.pmapi.bean.common.TransactionCorrelationId;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LogManager.getLogger(NotificationServiceImpl.class);
    private OneyHttpClient httpClient;

    public NotificationServiceImpl() {
        httpClient = OneyHttpClient.getInstance();
    }

    @Override
    public NotificationResponse parse(NotificationRequest notificationRequest) {
        // init data
        String partnerTransactionId = "UNKNOWN";
        String paymentStatus = "UNKNOWN";
        PaymentResponse paymentResponse;


        // get the body of the request
        try (BufferedReader br = new BufferedReader(new InputStreamReader(notificationRequest.getContent(), "UTF-8"))) {
            String bodyResponse = br.lines().collect(Collectors.joining(System.lineSeparator()));
            final String key = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, OneyConstants.PARTNER_CHIFFREMENT_KEY);

            // extract all needed data
            OneyNotificationResponse oneyResponse = OneyNotificationResponse.createTransactionStatusResponseFromJson(bodyResponse, key);
            partnerTransactionId = oneyResponse.getPurchase().getExternalReference();
            paymentStatus = oneyResponse.getPurchase().getStatusCode();

            // create a template success response
            PaymentResponse successPaymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                    .withStatusCode(Integer.toString(HTTP_OK))
                    .withTransactionDetails(new EmptyTransactionDetails())
                    .withPartnerTransactionId(partnerTransactionId)
                    .withMessage(new Message(Message.MessageType.SUCCESS, oneyResponse.getPurchase().getStatusLabel()))
                    .build();


            // check the payment status
            switch (paymentStatus) {
                case "FUNDED":
                case "TO_BE_FUNDED":
                case "CANCELLED":
                    paymentResponse = successPaymentResponse;
                    break;
                case "FAVORABLE":
                    // if captureNow => does to comfirm call
                    if (PluginUtils.isCaptureNow(oneyResponse.getMerchantContext())) {
                        // confirm the request
                        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(notificationRequest, oneyResponse)
                                .build();
                        StringResponse confirmResponse = httpClient.initiateConfirmationPayment(confirmRequest, notificationRequest.getEnvironment().isSandbox());

                        // check the confirmation response
                        if (confirmResponse.getContent() == null || confirmResponse.getCode() != HTTP_OK) {
                            // unable to read the response
                            String errorMessage ="Unable to read the confirmation response";
                            LOGGER.error(errorMessage);
                            paymentResponse = OneyErrorHandler.getPaymentResponseFailure(FailureCause.COMMUNICATION_ERROR, partnerTransactionId, PluginUtils.truncate(errorMessage, 50));
                            break;
                        }

                        TransactionStatusResponse confirmTransactionResponse = createTransactionStatusResponseFromJson(confirmResponse.getContent(), key);
                        if (confirmTransactionResponse == null || confirmTransactionResponse.getStatusPurchase() == null) {
                            // unable to read the payment status
                            String errorMessage = "Unable to read the confirmation response transaction status";
                            LOGGER.error(errorMessage);
                            paymentResponse = OneyErrorHandler.getPaymentResponseFailure(FailureCause.COMMUNICATION_ERROR, partnerTransactionId, PluginUtils.truncate(errorMessage, 50));
                            break;
                        }

                        // check the confirmation response status
                        String confirmStatus = confirmTransactionResponse.getStatusPurchase().getStatusCode();
                        if ("FUNDED".equals(confirmStatus)) {
                            // success
                            paymentResponse = successPaymentResponse;
                        } else {
                            paymentResponse = OneyErrorHandler.getPaymentResponseFailure(FailureCause.REFUSED, partnerTransactionId, "payment not funded");
                        }
                    } else {
                        // is NOT to capture now
                        paymentResponse = successPaymentResponse;
                    }

                    break;
                case "REFUSED":
                    paymentResponse = OneyErrorHandler.getPaymentResponseFailure(FailureCause.REFUSED,
                            partnerTransactionId, PluginUtils.truncate(oneyResponse.getPurchase().getStatusLabel(), 50));
                    break;
                case "ABORTED":
                    paymentResponse = OneyErrorHandler.getPaymentResponseFailure(FailureCause.CANCEL,
                            partnerTransactionId, PluginUtils.truncate(oneyResponse.getPurchase().getStatusLabel(), 50));
                    break;
                case "PENDING":
                    paymentResponse = PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                            .withPartnerTransactionId(partnerTransactionId)
                            .withOnHoldCause(OnHoldCause.SCORING_ASYNC)
                            .build();
                    break;
                default:
                    // Ignore the notification, with a 204 HTTP status code
                    LOGGER.info("Unknown payment status: " + paymentStatus);
                    return IgnoreNotificationResponse.IgnoreNotificationResponseBuilder.aIgnoreNotificationResponseBuilder()
                            .withHttpStatus(204)
                            .build();
            }

        } catch (IOException e) {
            String errorMessage = "Unable to read the notification request's body";
            LOGGER.error(errorMessage);
            paymentResponse = OneyErrorHandler.getPaymentResponseFailure(FailureCause.COMMUNICATION_ERROR, partnerTransactionId, PluginUtils.truncate(errorMessage, 50));
        } catch (PluginTechnicalException e) {
            LOGGER.error("Unable to get information needed to know the transaction status");
            paymentResponse = OneyErrorHandler.getPaymentResponseFailure(e.getFailureCause(), partnerTransactionId, e.getTruncatedErrorCodeOrLabel());
        }

        // create response
        TransactionCorrelationId transactionCorrelationId = TransactionCorrelationId.TransactionCorrelationIdBuilder.aCorrelationIdBuilder()
                .withType(TransactionCorrelationId.CorrelationIdType.PARTNER_TRANSACTION_ID)
                .withValue(partnerTransactionId)
                .build();

        return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder.aPaymentResponseByNotificationResponseBuilder()
                .withPaymentResponse(paymentResponse)
                .withTransactionCorrelationId(transactionCorrelationId)
                .withHttpStatus(204)
                .build();
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        //ras.
    }
}
