package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.exception.InvalidRequestException;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.PluginUtils;
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
import com.payline.pmapi.service.PaymentWithRedirectionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.payline.payment.oney.bean.response.PaymentErrorResponse.paymentErrorResponseFromJson;
import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;
import static com.payline.payment.oney.utils.OneyErrorHandler.handleOneyFailureResponse;
import static com.payline.pmapi.bean.common.Message.MessageType.SUCCESS;

public class PaymentWithRedirectionServiceImpl implements PaymentWithRedirectionService {

    private static final Logger LOGGER = LogManager.getLogger(PaymentWithRedirectionServiceImpl.class);
    private OneyHttpClient httpClient;

    public PaymentWithRedirectionServiceImpl() {
        this.httpClient = OneyHttpClient.getInstance();
    }

    @Override
    public PaymentResponse finalizeRedirectionPayment(RedirectionPaymentRequest redirectionPaymentRequest) {

        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        try {
            return validatePayment(confirmRequest);

        } catch (IOException | URISyntaxException | DecryptException | InvalidRequestException e) {
            LOGGER.error("unable to confirm the payment", e);
            return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withFailureCause(FailureCause.COMMUNICATION_ERROR)
                    .withErrorCode("503")
                    .withPartnerTransactionId(confirmRequest.getPurchaseReference())
                    .build();
        }

    }

    @Override
    public PaymentResponse handleSessionExpired(TransactionStatusRequest transactionStatusRequest) {

        OneyTransactionStatusRequest oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .fromTransactionStatusRequest(transactionStatusRequest)
                .build();
        try {
            StringResponse status = this.httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest);

            //l'appel est OK on gere selon la response
            if (status.getCode() == HTTP_OK) {
                TransactionStatusResponse response = TransactionStatusResponse.createTransactionStatusResponseFromJson(status.getContent(), oneyTransactionStatusRequest.getEncryptKey());
                if (response.getStatusPurchase() != null) {
                    switch (response.getStatusPurchase().getStatusCode()) {
                        //renvoi d'un paymentResponseOnHold
                        case "PENDING":
                            //renvoi d'une PaymentResponseOnHold ??
                            return PaymentResponseOnHold.PaymentResponseOnHoldBuilder.aPaymentResponseOnHold()
                                    .withPartnerTransactionId(transactionStatusRequest.getTransactionId())
                                    .withOnHoldCause(OnHoldCause.SCORING_ASYNC)
                                    .build();
                        case "FAVORABLE":
                            //confirmer la demande
                            OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(transactionStatusRequest)
                                    .build();

                            return this.validatePayment(confirmRequest);

                        case "FUNDED":
                            // demande deja acceptée renvoyer une paymentResponseSuccess avec donnees
                            return PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                                    .withStatusCode("200")
                                    .withMessage(new Message(Message.MessageType.SUCCESS, "OK"))
                                    .withPartnerTransactionId(oneyTransactionStatusRequest.getPurchaseReference())
                                    .withTransactionDetails(new EmptyTransactionDetails())
                                    .build();
                        case "REFUSED":
                            return OneyErrorHandler.getPaymentResponseFailure(FailureCause.REFUSED, oneyTransactionStatusRequest.getPurchaseReference());
                        case "ABORTED":
                        case "CANCELLED":
                            //demande rejetee ou annuléee ou
                            //change value
                            return OneyErrorHandler.getPaymentResponseFailure(FailureCause.CANCEL, oneyTransactionStatusRequest.getPurchaseReference());
                        default:
                            return OneyErrorHandler.getPaymentResponseFailure(FailureCause.CANCEL, oneyTransactionStatusRequest.getPurchaseReference());
                    }
                } else {
                    //Pas de statut pour cette demande
                    return OneyErrorHandler.getPaymentResponseFailure(FailureCause.CANCEL, oneyTransactionStatusRequest.getPurchaseReference());
                }

            } else {
                return OneyErrorHandler.getPaymentResponseFailure(FailureCause.CANCEL, oneyTransactionStatusRequest.getPurchaseReference());
            }


        } catch (IOException | DecryptException | URISyntaxException | InvalidRequestException e) {
            LOGGER.error("unable to handle the session expiration", e);
            //Renvoyer une erreur
            return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }


    }

    /**
     * Effectue l'appel http permettant de confirmer une commande
     *
     * @return
     */
    public PaymentResponse validatePayment(OneyConfirmRequest confirmRequest) throws
            IOException, URISyntaxException, DecryptException, InvalidRequestException {

        StringResponse oneyResponse = httpClient.initiateConfirmationPayment(confirmRequest);
        // si erreur lors de l'envoi de la requete http
        if (oneyResponse == null) {
            LOGGER.debug("oneyResponse StringResponse is null !");
            LOGGER.error("Payment is null");
            return OneyErrorHandler.getPaymentResponseFailure(FailureCause.INTERNAL_ERROR, confirmRequest.getPurchaseReference());

        }
        //si erreur dans la requete http
        if (oneyResponse.getCode() != HTTP_OK) {
            OneyFailureResponse failureResponse = new OneyFailureResponse(oneyResponse.getCode(), oneyResponse.getMessage(), oneyResponse.getContent(), paymentErrorResponseFromJson(oneyResponse.getContent()));
            LOGGER.error("Payment failed {} ", failureResponse.getContent());

            return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                    .withFailureCause(handleOneyFailureResponse(failureResponse))
                    .withErrorCode(failureResponse.getCode().toString())
                    .build();
        }
        //Confirmation OK, on traite la reponse
        else {
            //On dechiffre la response
            TransactionStatusResponse responseDecrypted = createTransactionStatusResponseFromJson(oneyResponse.getContent(), confirmRequest.getEncryptKey());
            //Si Oney renvoie une message vide, on renvoi un Payment Failure response
            if (responseDecrypted.getStatusPurchase() == null) {
                LOGGER.debug("oneyResponse StringResponse is null !");
                LOGGER.error("Payment is null");
                return OneyErrorHandler.getPaymentResponseFailure(FailureCause.REFUSED, confirmRequest.getPurchaseReference());
            }

            //definir les additionals data a renvoyer
            //Additional data  : ajouter purchaseReference ? amount ? transaction status ??
            String message = (responseDecrypted.getStatusPurchase() == null) ? "" : responseDecrypted.getStatusPurchase().getStatusLabel();


            return PaymentResponseSuccess.PaymentResponseSuccessBuilder.aPaymentResponseSuccess()
                    .withTransactionAdditionalData(responseDecrypted.toString())
                    .withPartnerTransactionId(PluginUtils.parseReference(confirmRequest.getPurchaseReference()))
                    .withStatusCode(String.valueOf(oneyResponse.getCode()))
                    .withMessage(new Message(SUCCESS, message))
                    .withTransactionDetails(new EmptyTransactionDetails())
                    .build();
        }
    }
}
