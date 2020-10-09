package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.PurchaseNotification;
import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.OneyNotificationResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.*;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.FUNDED;
import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.TO_BE_FUNDED;
import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;

public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOGGER = LogManager.getLogger(NotificationServiceImpl.class);
    private OneyHttpClient httpClient;

    public NotificationServiceImpl() {
        httpClient = OneyHttpClient.getInstance();
    }

    @Override
    public NotificationResponse parse(NotificationRequest request) {
        NotificationResponse notificationResponse;

        /*
         * Initialize a NotificationResponseHandler object, given the value of transactionId.
         * This will determine the type of NotificationResponse returned :
         *   if transactionId is null, this method should return an instance of PaymentResponseByNotificationResponse,
         *   if transactionId is not null, it should return an instance of TransactionStateChangedResponse.
         * The current method contains only the logical operations performed on the input notification.
         * The building of the appropriate objects to return is delegated to the NotificationResponseHandler instance.
         */
        final String transactionId = request.getTransactionId();
        NotificationResponseHandler notificationResponseHandler;
        if (transactionId == null) {
            // transaction does not exist yet in Payline -> PaymentResponseByNotificationResponse will be returned.
            notificationResponseHandler = new PaymentResponseByNotificationResponseHandler();
        } else {
            // transaction exists in Payline -> TransactionStateChangedResponse will be returned.
            notificationResponseHandler = new TransactionStateChangedResponseHandler();
        }

        // Initialize partner transaction ID
        String partnerTransactionId = "UNKNOWN";

        try {
            // retrieve ciphering key
            final String key = RequestConfigServiceImpl.INSTANCE.getParameterValue(request, OneyConstants.PARTNER_CHIFFREMENT_KEY);

            // read body from request
            BufferedReader br = new BufferedReader(new InputStreamReader(request.getContent(), StandardCharsets.UTF_8));
            String bodyResponse = br.lines().collect(Collectors.joining(System.lineSeparator()));

            // create an OneyResponse object from json String
            OneyNotificationResponse oneyResponse = OneyNotificationResponse.createTransactionStatusResponseFromJson(bodyResponse, key);

            // validate the transactionId to the notification's PspContext
            if (transactionId != null && !transactionId.equals(oneyResponse.getPspContext())) {
                LOGGER.info("Given transactionId doesn't match psp_context");
                throw new InvalidDataException("Given transactionId doesn't match psp_context");
            }

            // check the integrity of the notification content
            if (oneyResponse.getPurchase() == null
                    || oneyResponse.getPurchase().getStatusCode() == null
                    || oneyResponse.getPurchase().getExternalReference() == null
                    || oneyResponse.getMerchantContext() == null) {
                throw new InvalidDataException("The notification content is missing required data");
            }

            // extract data from the notification content
            PurchaseNotification purchase = oneyResponse.getPurchase();
            partnerTransactionId = purchase.getExternalReference();
            PurchaseStatus.StatusCode paymentStatus = purchase.getStatusCode();
            Boolean isCaptureNow = PluginUtils.isCaptureNow(oneyResponse.getMerchantContext());

            // analyze the payment status
            switch (paymentStatus) {
                case FUNDED:
                case TO_BE_FUNDED:
                case CANCELLED:
                    notificationResponse = notificationResponseHandler.successResponse(oneyResponse, transactionId);
                    break;

                case FAVORABLE:
                    if (Boolean.TRUE.equals(isCaptureNow)) {
                        // if captureNow => do the confirmation call
                        PurchaseStatus.StatusCode status = confirmAndCheck(request, oneyResponse);
                        if (FUNDED.equals(status) || TO_BE_FUNDED.equals(status)) {
                            notificationResponse = notificationResponseHandler.successResponse(oneyResponse, transactionId);
                        } else {
                            notificationResponse = notificationResponseHandler.failureResponse(oneyResponse, transactionId,
                                    FailureCause.REFUSED, "payment not funded after confirmation");
                        }
                    } else {
                        // do NOT to capture now
                        notificationResponse = notificationResponseHandler.successResponse(oneyResponse, transactionId);
                    }

                    break;
                case REFUSED:
                    notificationResponse = notificationResponseHandler.failureResponse(oneyResponse, transactionId,
                            FailureCause.REFUSED, oneyResponse.getPurchase().getStatusLabel());
                    break;
                case ABORTED:
                    notificationResponse = notificationResponseHandler.failureResponse(oneyResponse, transactionId,
                            FailureCause.CANCEL, oneyResponse.getPurchase().getStatusLabel());
                    break;
                case PENDING:
                    notificationResponse = notificationResponseHandler.onHoldResponse(oneyResponse, transactionId);
                    break;
                default:
                    // Ignore the notification, with a 204 HTTP status code
                    LOGGER.info("Unknown payment status: {}", paymentStatus);
                    return IgnoreNotificationResponse.IgnoreNotificationResponseBuilder.aIgnoreNotificationResponseBuilder()
                            .withHttpStatus(204)
                            .build();
            }
        } catch (PluginTechnicalException e) {
            notificationResponse = notificationResponseHandler.handlePluginTechnicalException(e, transactionId, partnerTransactionId);
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            notificationResponse = notificationResponseHandler.handleRuntimeException(e, transactionId, partnerTransactionId);
        }

        return notificationResponse;
    }

    /**
     * Send the confirmation call to the partner API.
     * The, send another request to retrieve the final status of the payment. This final status is then returned.
     *
     * @param request      the notification request received from Payline core
     * @param oneyResponse the parsed content of the notification
     * @return the final status of the payment, after confirmation.
     * @throws PluginTechnicalException
     */
    private PurchaseStatus.StatusCode confirmAndCheck(NotificationRequest request, OneyNotificationResponse oneyResponse) throws PluginTechnicalException {
        final String key = RequestConfigServiceImpl.INSTANCE.getParameterValue(request, OneyConstants.PARTNER_CHIFFREMENT_KEY);

        // confirmation
        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(request, oneyResponse).build();
        httpClient.initiateConfirmationPayment(confirmRequest, request.getEnvironment().isSandbox());


        // check
        OneyTransactionStatusRequest oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .fromNotificationRequest(request)
                .withPurchaseReference(PluginUtils.fullPurchaseReference(oneyResponse.getPurchase().getExternalReference()))
                .build();
        PurchaseStatus.StatusCode finalStatus = null;
        int attempts = 0;
        while (finalStatus == null) {
            StringResponse checkStatusResponse = httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest, request.getEnvironment().isSandbox());
            attempts++;

            // verify the response integrity and HTTP status
            if (checkStatusResponse.getContent() == null) {
                String message = "Unable to read the check response";
                LOGGER.error(message);
                throw new HttpCallException(message, "empty check response content");
            }
            if (checkStatusResponse.getCode() != HTTP_OK) {
                String message = "bad response to the check request";
                LOGGER.error(message);
                throw new InvalidDataException(message, "bad responseCode");
            }

            // Parse the response content
            TransactionStatusResponse statusResponseResponse = createTransactionStatusResponseFromJson(checkStatusResponse.getContent(), key);
            if (statusResponseResponse == null || statusResponseResponse.getStatusPurchase() == null) {
                // unable to read the payment status
                String message = "Unable to read the confirmation response transaction status";
                LOGGER.error(message);
                throw new HttpCallException(message, "empty check response object or statusCode");
            }

            // Retry every 3s until the payment status equals FUNDED or TO_BE_FUNDED (3 times max)
            PurchaseStatus.StatusCode currentStatus = statusResponseResponse.getStatusPurchase().getStatusCode();
            if (attempts == 3 || FUNDED.equals(currentStatus) || TO_BE_FUNDED.equals(currentStatus)) {
                finalStatus = currentStatus;
            } else {
                // if it was not the third attempt, wait for 3s before the next one
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    LOGGER.error("The thread has been interrupted. Shutting down the thread cleanly...");
                    Thread.currentThread().interrupt();
                }
            }
        }

        return finalStatus;
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        //ras.
    }

    /**
     * Define the methods of the classes which will build the instances of {@link NotificationResponse} to return.
     */
    private interface NotificationResponseHandler {
        NotificationResponse successResponse(OneyNotificationResponse notificationContent, String transactionId);

        NotificationResponse failureResponse(OneyNotificationResponse notificationContent, String transactionId,
                                             FailureCause failureCause, String errorCode);

        NotificationResponse onHoldResponse(OneyNotificationResponse notificationContent, String transactionId);

        NotificationResponse handlePluginTechnicalException(PluginTechnicalException e, String transactionId, String partnerTransactionId);

        NotificationResponse handleRuntimeException(RuntimeException e, String transactionId, String partnerTransactionId);
    }

    /**
     * Build {@link PaymentResponseByNotificationResponse} instances.
     */
    private static class PaymentResponseByNotificationResponseHandler implements NotificationResponseHandler {

        @Override
        public NotificationResponse successResponse(OneyNotificationResponse notificationContent, String transactionId) {
            String partnerTransactionId = notificationContent.getPurchase().getExternalReference();
            PaymentResponse paymentResponse = PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                    .withStatusCode(Integer.toString(HTTP_OK))
                    .withTransactionDetails(new EmptyTransactionDetails())
                    .withPartnerTransactionId(partnerTransactionId)
                    .withMessage(new Message(Message.MessageType.SUCCESS, notificationContent.getPurchase().getStatusLabel()))
                    .build();

            return this.buildResponse(paymentResponse, partnerTransactionId);
        }

        @Override
        public NotificationResponse failureResponse(OneyNotificationResponse notificationContent, String transactionId,
                                                    FailureCause failureCause, String errorCode) {
            String partnerTransactionId = notificationContent.getPurchase().getExternalReference();
            PaymentResponse paymentResponse = OneyErrorHandler.getPaymentResponseFailure(failureCause,
                    partnerTransactionId, PluginUtils.truncate(notificationContent.getPurchase().getStatusLabel(), 50));

            return this.buildResponse(paymentResponse, partnerTransactionId);
        }

        @Override
        public NotificationResponse onHoldResponse(OneyNotificationResponse notificationContent, String transactionId) {
            String partnerTransactionId = notificationContent.getPurchase().getExternalReference();
            PaymentResponse paymentResponse = PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withOnHoldCause(OnHoldCause.SCORING_ASYNC)
                    .build();

            return this.buildResponse(paymentResponse, partnerTransactionId);
        }

        @Override
        public NotificationResponse handlePluginTechnicalException(PluginTechnicalException e, String transactionId,
                                                                   String partnerTransactionId) {
            return this.buildResponse(e.toPaymentResponseFailure(), partnerTransactionId);
        }

        @Override
        public NotificationResponse handleRuntimeException(RuntimeException e, String transactionId,
                                                           String partnerTransactionId) {
            PaymentResponse paymentResponse = PaymentResponseFailure.PaymentResponseFailureBuilder
                    .aPaymentResponseFailure()
                    .withErrorCode(PluginTechnicalException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();

            return this.buildResponse(paymentResponse, partnerTransactionId);
        }

        private PaymentResponseByNotificationResponse buildResponse(PaymentResponse paymentResponse, String partnerTransactionId) {
            return PaymentResponseByNotificationResponse.PaymentResponseByNotificationResponseBuilder.aPaymentResponseByNotificationResponseBuilder()
                    .withPaymentResponse(paymentResponse)
                    .withTransactionCorrelationId(
                            TransactionCorrelationId.TransactionCorrelationIdBuilder
                                    .aCorrelationIdBuilder()
                                    .withType(TransactionCorrelationId.CorrelationIdType.PARTNER_TRANSACTION_ID)
                                    .withValue(partnerTransactionId)
                                    .build()
                    )
                    .withHttpStatus(204)
                    .build();
        }
    }

    /**
     * Build {@link TransactionStateChangedResponse} instances.
     */
    private static class TransactionStateChangedResponseHandler implements NotificationResponseHandler {

        @Override
        public NotificationResponse successResponse(OneyNotificationResponse notificationContent, String transactionId) {
            return this.buildResponse(notificationContent, SuccessTransactionStatus.builder().build(), transactionId);
        }

        @Override
        public NotificationResponse failureResponse(OneyNotificationResponse notificationContent, String transactionId,
                                                    FailureCause failureCause, String errorCode) {
            return this.buildResponse(notificationContent, FailureTransactionStatus.builder().failureCause(failureCause).build(), transactionId);

        }

        @Override
        public NotificationResponse onHoldResponse(OneyNotificationResponse notificationContent, String transactionId) {
            return this.buildResponse(notificationContent, OnHoldTransactionStatus.builder().onHoldCause(OnHoldCause.SCORING_ASYNC).build(), transactionId);
        }

        @Override
        public NotificationResponse handlePluginTechnicalException(PluginTechnicalException e, String transactionId,
                                                                   String partnerTransactionId) {
            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(FailureTransactionStatus.builder().failureCause(e.getFailureCause()).build())
                    .withStatusDate(new Date())
                    .withHttpStatus(204)
                    .build();
        }

        @Override
        public NotificationResponse handleRuntimeException(RuntimeException e, String transactionId,
                                                           String partnerTransactionId) {
            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                    .withPartnerTransactionId(partnerTransactionId)
                    .withTransactionId(transactionId)
                    .withTransactionStatus(FailureTransactionStatus.builder().failureCause(FailureCause.INTERNAL_ERROR).build())
                    .withStatusDate(new Date())
                    .withHttpStatus(204)
                    .build();
        }

        private TransactionStateChangedResponse buildResponse(OneyNotificationResponse notificationContent, TransactionStatus status,
                                                              String transactionId) {
            String statusDetails = notificationContent.getPurchase().getReasonCode() + "-" + notificationContent.getPurchase().getReasonLabel();
            boolean isCaptureNow = PluginUtils.isCaptureNow(notificationContent.getMerchantContext());
            return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder
                    .aTransactionStateChangedResponse()
                    .withPartnerTransactionId(notificationContent.getPurchase().getExternalReference())
                    .withTransactionId(transactionId)
                    .withTransactionStatus(status)
                    .withStatusDate(new Date())
                    .withHttpStatus(204)
                    .withAction(isCaptureNow ? TransactionStateChangedResponse.Action.AUTHOR_AND_CAPTURE : TransactionStateChangedResponse.Action.AUTHOR)
                    .withStatusDetails(statusDetails)
                    .build();
        }
    }
}
