package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.exception.PluginTechnicalException;
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
        OneyConfirmRequest confirmRequest = null;
        try {
            confirmRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest).build();

            return validatePayment(confirmRequest);

        } catch (InvalidDataException e) {
            LOGGER.error("unable to confirm the payment", e);
            return e.toPaymentResponseFailure();

        } catch (PluginTechnicalException e) {
            LOGGER.error("unable to confirm the payment", e);
            return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withFailureCause(FailureCause.COMMUNICATION_ERROR)
                    .withErrorCode("503")
                    .withPartnerTransactionId(confirmRequest != null ? confirmRequest.getPurchaseReference() : "")
                    .build();
        }

    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {
        OneyTransactionStatusRequest oneyTransactionStatusRequest = null;
        try {
            oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .fromTransactionStatusRequest(transactionStatusRequest)
                    .build();
            StringResponse status = this.httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest);

            //l'appel est OK on gere selon la response
            if (status.getCode() == HTTP_OK) {
                TransactionStatusResponse response = TransactionStatusResponse.createTransactionStatusResponseFromJson(status.getContent(), oneyTransactionStatusRequest.getEncryptKey());
                if (response.getStatusPurchase() != null) {
                    // Special case in which we need to send a confirmation request
                    if( "FAVORABLE".equals( response.getStatusPurchase().getStatusCode() ) ){
                        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(transactionStatusRequest)
                                .build();
                        return this.validatePayment( confirmRequest );
                    }
                    else {
                        return this.handleTransactionStatusResponse( response,
                                oneyTransactionStatusRequest.getPurchaseReference() );
                    }
                } else {
                    //Pas de statut pour cette demande
                    return OneyErrorHandler.getPaymentResponseFailure(
                            FailureCause.CANCEL,
                            oneyTransactionStatusRequest.getPurchaseReference(),
                            ERROR_CODE + "null");
                }

            } else {
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.CANCEL,
                        oneyTransactionStatusRequest.getPurchaseReference(),
                        "HTTP return code " + status.getCode());
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
    public PaymentResponse validatePayment(OneyConfirmRequest confirmRequest) throws PluginTechnicalException {

        StringResponse oneyResponse = httpClient.initiateConfirmationPayment(confirmRequest);

        // si erreur lors de l'envoi de la requete http
        if (oneyResponse == null) {
            LOGGER.debug("oneyResponse StringResponse is null !");
            LOGGER.error("Payment is null");
            return OneyErrorHandler.getPaymentResponseFailure(
                    FailureCause.PARTNER_UNKNOWN_ERROR,
                    confirmRequest.getPurchaseReference(),
                    "Empty partner response"
            );

        }
        try {
            //si erreur dans la requete http
            if (oneyResponse.getCode() != HTTP_OK) {
                OneyFailureResponse failureResponse = new OneyFailureResponse(oneyResponse.getCode(), oneyResponse.getMessage(), oneyResponse.getContent(), paymentErrorResponseFromJson(oneyResponse.getContent()));
                LOGGER.error("Payment failed {} ", failureResponse.getContent());

                return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                        .withFailureCause(handleOneyFailureResponse(failureResponse))
                        .withErrorCode(failureResponse.toPaylineErrorCode())
                        .build();
            }
            //Confirmation OK, on traite la reponse
            else {
                TransactionStatusResponse responseDecrypted = createTransactionStatusResponseFromJson(oneyResponse.getContent(), confirmRequest.getEncryptKey());

                if( responseDecrypted == null || responseDecrypted.getStatusPurchase() == null ){
                    LOGGER.error("Transaction status response or purchase status is null");
                    return OneyErrorHandler.getPaymentResponseFailure(
                            FailureCause.REFUSED,
                            confirmRequest.getPurchaseReference(),
                            ERROR_CODE + "null");
                }
                return this.handleTransactionStatusResponse( responseDecrypted, confirmRequest.getPurchaseReference() );
            }
        }
        catch (PluginTechnicalException e) {
            return e.toPaymentResponseFailure();
        }
    }

    private PaymentResponse handleTransactionStatusResponse( TransactionStatusResponse response,
                                                             String purchaseReference ){
        PurchaseStatus purchaseStatus = response.getStatusPurchase();
        switch( purchaseStatus.getStatusCode() ){
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
                        .withStatusCode( Integer.toString(HTTP_OK) )
                        .withTransactionDetails(new EmptyTransactionDetails())
                        .withPartnerTransactionId( purchaseReference )
                        .withMessage( new Message(Message.MessageType.SUCCESS, message) )
                        .withTransactionAdditionalData( response.toString() )
                        .build();
            default:
                // Should not be encountered !
                LOGGER.error("Unexpected purchase status code encountered: " + purchaseStatus.getStatusCode() );
                return OneyErrorHandler.getPaymentResponseFailure(
                        FailureCause.PARTNER_UNKNOWN_ERROR,
                        purchaseReference,
                        "Unexpected purchase status: " + purchaseStatus.getStatusCode()
                );
        }
    }

    private String addErrorCode(TransactionStatusResponse response) {
        return ERROR_CODE + response.getStatusPurchase().getStatusCode();
    }
}
