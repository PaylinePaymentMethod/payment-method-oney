package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.Message;
import com.payline.pmapi.bean.common.OnHoldCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.buyerpaymentidentifier.impl.EmptyTransactionDetails;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseActiveWaiting;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.logging.log4j.Logger;

import static com.payline.payment.oney.bean.response.PaymentErrorResponse.paymentErrorResponseFromJson;
import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;
import static com.payline.payment.oney.utils.OneyErrorHandler.handleOneyFailureResponse;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);
    private OneyHttpClient httpClient;

    private static final String ERROR_CODE = "Purchase status : ";

    public PaymentWithRedirectionServiceImpl() {
        this.httpClient = OneyHttpClient.getInstance();
    }

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {
        return new PaymentResponseActiveWaiting();
    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        String externalReference = transactionStatusRequest.getOrder().getReference();
        try {
            OneyTransactionStatusRequest oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .fromTransactionStatusRequest(transactionStatusRequest)
                    .build();
            StringResponse status = this.httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest, transactionStatusRequest.getEnvironment().isSandbox());

            PaymentResponse paymentResponse = findErrorResponse(status, externalReference, oneyTransactionStatusRequest.getEncryptKey());
            if (paymentResponse != null) {
                return paymentResponse;
            } else {
                // Special case in which we need to send a confirmation request
                TransactionStatusResponse response = TransactionStatusResponse.createTransactionStatusResponseFromJson(status.getContent(), oneyTransactionStatusRequest.getEncryptKey());
                if (response.getStatusPurchase() != null) {
                    // Special case in which we need to send a confirmation request
                    if ("FAVORABLE".equals(response.getStatusPurchase().getStatusCode())) {
                        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(transactionStatusRequest)
                                .build();
                        return this.validatePayment(confirmRequest, transactionStatusRequest.getEnvironment().isSandbox(), externalReference);
                    } else {
                        return this.handleTransactionStatusResponse(response, externalReference);
                    }
                } else {
                    //Pas de statut pour cette demande
                    return OneyErrorHandler.getPaymentResponseFailure(
                            FailureCause.CANCEL,
                            externalReference,
                            ERROR_CODE + "null");
                }
            }

        } catch (PluginTechnicalException e) {
            return e.toPaymentResponseFailure();
        }
    }


    /**
     * Effectue l'appel http permettant de confirmer une commande
     *
     * @return PaymentResponse
     */
    public PaymentResponse validatePayment(OneyConfirmRequest confirmRequest, boolean isSandbox, String partnerTransactionId) throws PluginTechnicalException {
        LOGGER.info("payment confirmation request nedeed");
        StringResponse oneyResponse = httpClient.initiateConfirmationPayment(confirmRequest, isSandbox);

        // si erreur lors de l'envoi de la requete http
        if (oneyResponse == null) {
            LOGGER.debug("oneyResponse StringResponse is null !");
            LOGGER.error("Payment is null");
            return OneyErrorHandler.getPaymentResponseFailure(
                    FailureCause.PARTNER_UNKNOWN_ERROR,
                    partnerTransactionId,
                    "Empty partner response"
            );

        }
        try {
            //si erreur dans la requete http
            PaymentResponse paymentResponse = findErrorResponse(oneyResponse, partnerTransactionId, confirmRequest.getEncryptKey());
            if (paymentResponse != null) {
                return paymentResponse;
            } else {
                TransactionStatusResponse responseDecrypted = createTransactionStatusResponseFromJson(oneyResponse.getContent(), confirmRequest.getEncryptKey());

                if (responseDecrypted == null || responseDecrypted.getStatusPurchase() == null) {
                    LOGGER.error("Transaction status response or purchase status is null");
                    return OneyErrorHandler.getPaymentResponseFailure(
                            FailureCause.REFUSED,
                            partnerTransactionId,
                            ERROR_CODE + "null");
                }

                return this.handleTransactionStatusResponse(responseDecrypted, partnerTransactionId);
            }
        } catch (PluginTechnicalException e) {
            return e.toPaymentResponseFailure();
        }
    }

    private PaymentResponse handleTransactionStatusResponse(TransactionStatusResponse response,
                                                            String purchaseReference) {
        PurchaseStatus purchaseStatus = response.getStatusPurchase();
        switch (purchaseStatus.getStatusCode()) {
            case "PENDING":
                // Payline: PENDING
                return PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                        .withPartnerTransactionId(purchaseReference)
                        .withOnHoldCause(OnHoldCause.SCORING_ASYNC)
                        .build();
            case "REFUSED":
                // Payline: REFUSED
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.REFUSED,
                        purchaseReference,
                        addErrorCode(response)
                );
            case "ABORTED":
                // Payline:
                // CANCEL or SESSION_EXPIRED according to Confluence mapping.
                // Always CANCEL according to the former code...
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.CANCEL,
                        purchaseReference,
                        addErrorCode(response)
                );
            case "FAVORABLE":
            case "FUNDED":
            case "CANCELLED":
            case "TO_BE_FUNDED":
                // Payline: ACCEPTED
                String message = purchaseStatus.getStatusLabel() != null ? purchaseStatus.getStatusLabel() : "OK";
                return PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                        .withStatusCode(Integer.toString(HTTP_OK))
                        .withTransactionDetails(new EmptyTransactionDetails())
                        .withPartnerTransactionId(purchaseReference)
                        .withMessage(new Message(Message.MessageType.SUCCESS, message))
                        .withTransactionAdditionalData(response.toString())
                        .build();
            default:
                // Should not be encountered !
                LOGGER.error("Unexpected purchase status code encountered: " + purchaseStatus.getStatusCode());
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.PARTNER_UNKNOWN_ERROR,
                        purchaseReference,
                        "Unexpected purchase status: " + purchaseStatus.getStatusCode()
                );
        }
    }

    PaymentResponse findErrorResponse(StringResponse response, String partnerTransactionID, String key) throws PluginTechnicalException {
        //si erreur dans la requete http
        if (response.getCode() != HTTP_OK) {
            OneyFailureResponse failureResponse = new OneyFailureResponse(response.getCode(),
                    response.getMessage(),
                    response.getContent(),
                    paymentErrorResponseFromJson(response.getContent()));
            LOGGER.error("Payment failed {} ", failureResponse.getContent());

            return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withFailureCause(handleOneyFailureResponse(failureResponse))
                    .withErrorCode(failureResponse.toPaylineErrorCode())
                    .build();
        }
        //Confirmation OK, on traite la reponse
        else {
            TransactionStatusResponse responseDecrypted = createTransactionStatusResponseFromJson(response.getContent(), key);

            if (responseDecrypted == null || responseDecrypted.getStatusPurchase() == null) {
                LOGGER.error("Transaction status response or purchase status is null");
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.REFUSED,
                        partnerTransactionID,
                        ERROR_CODE + "null");
            }
            return this.handleTransactionStatusResponse(responseDecrypted, partnerTransactionID);
        }
    }

    private String addErrorCode(TransactionStatusResponse response) {
        return ERROR_CODE + response.getStatusPurchase().getStatusCode();
    }
}
