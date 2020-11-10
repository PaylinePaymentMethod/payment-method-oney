package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.bean.request.OneyRefundRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.RefundService;
import org.apache.logging.log4j.Logger;

import static com.payline.payment.oney.bean.response.PaymentErrorResponse.paymentErrorResponseFromJson;
import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;
import static com.payline.payment.oney.utils.OneyErrorHandler.handleOneyFailureResponse;

public class RefundServiceImpl implements RefundService {

    private static final Logger LOGGER = LogManager.getLogger(RefundServiceImpl.class);

    @Override
    public RefundResponse refundRequest(RefundRequest refundRequest) {

        OneyRefundRequest oneyRefundRequest = null;
        try {
            //obtenir statut de la requete
            PurchaseStatus.StatusCode status = handleStatusRequest(refundRequest);
            //faire une  transactionStatusRequest
            boolean refundFlag = PluginUtils.getRefundFlag(status);

            //creation d'une OneyRefundRequest
            oneyRefundRequest = OneyRefundRequest.Builder.aOneyRefundRequest()
                    .fromRefundRequest(refundRequest, refundFlag)
                    .build();

            final OneyHttpClient httpClient = getNewHttpClientInstance(refundRequest);
            StringResponse oneyResponse = httpClient.initiateRefundPayment(oneyRefundRequest, refundRequest.getEnvironment().isSandbox());
            //handle Response
            if (oneyResponse == null) {
                LOGGER.debug("oneyResponse StringResponse is null !");
                LOGGER.error("Refund is null");
                return OneyErrorHandler.geRefundResponseFailure(
                        FailureCause.PARTNER_UNKNOWN_ERROR,
                        refundRequest.getPartnerTransactionId(),
                        "Empty partner response");
            }
            //si erreur dans la requete http
            if (oneyResponse.getCode() != HTTP_OK) {
                OneyFailureResponse failureResponse = new OneyFailureResponse(oneyResponse.getCode(), oneyResponse.getMessage(), oneyResponse.getContent(), paymentErrorResponseFromJson(oneyResponse.getContent()));
                LOGGER.error("Refund failed {} ", failureResponse.getContent());

                return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                        .withFailureCause(handleOneyFailureResponse(failureResponse))
                        .withErrorCode(failureResponse.toPaylineErrorCode())
                        .build();
            } else {
                //On dechiffre la response
                TransactionStatusResponse responseDecrypted = createTransactionStatusResponseFromJson(oneyResponse.getContent(), oneyRefundRequest.getEncryptKey());

                //Si Oney renvoie une message vide, on renvoi un Payment Failure response
                if (responseDecrypted.getStatusPurchase() == null) {
                    LOGGER.debug("oneyResponse StringResponse is null !");
                    LOGGER.error("Refund is null");
                    return OneyErrorHandler.geRefundResponseFailure(
                            FailureCause.REFUSED,
                            refundRequest.getPartnerTransactionId(),
                            "Purchase status : null");
                }

                LOGGER.info("Refund success");
                return RefundResponseSuccess.RefundResponseSuccessBuilder.aRefundResponseSuccess()
                        .withPartnerTransactionId(oneyRefundRequest.getPurchaseReference())
                        .withStatusCode(responseDecrypted.getStatusPurchase().getStatusCode().name())
                        .build();

            }
        } catch (PluginTechnicalException e) {
            LOGGER.error("unable init the refund", e);
            return OneyErrorHandler.geRefundResponseFailure(
                    e.getFailureCause(),
                    refundRequest.getPartnerTransactionId(),
                    e.getErrorCodeOrLabel());
        }catch (RuntimeException e) {
            LOGGER.error("Unexpected plugin error", e);
            return RefundResponseFailure.RefundResponseFailureBuilder
                    .aRefundResponseFailure()
                    .withErrorCode(PluginTechnicalException.runtimeErrorCode(e))
                    .withFailureCause(FailureCause.INTERNAL_ERROR)
                    .build();
        }

    }


    @Override
    public boolean canMultiple() {
        return true;
    }

    @Override
    public boolean canPartial() {
        return true;
    }

    /**
     * Obteniir le statut d'une transaction en cours
     *
     * @param refundRequest
     * @return
     */
    public PurchaseStatus.StatusCode handleStatusRequest(RefundRequest refundRequest) throws PluginTechnicalException {
        OneyTransactionStatusRequest oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .fromRefundRequest(refundRequest)
                .build();
        PurchaseStatus.StatusCode transactionStatusCode = null;
        try {
            final OneyHttpClient httpClient = getNewHttpClientInstance(refundRequest);
            StringResponse status = httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest, refundRequest.getEnvironment().isSandbox());
            //l'appel est OK on gere selon la response
            if (status.getCode() == HTTP_OK) {
                TransactionStatusResponse response = TransactionStatusResponse.createTransactionStatusResponseFromJson(status.getContent(), oneyTransactionStatusRequest.getEncryptKey());
                transactionStatusCode = response.getStatusPurchase() == null ? null : response.getStatusPurchase().getStatusCode();
            }

        } catch (PluginTechnicalException e) {
            LOGGER.error("unable to get transaction status", e);
            throw e;

        }
        return transactionStatusCode;

    }

    protected OneyHttpClient getNewHttpClientInstance(final RefundRequest refundRequest) {
        return OneyHttpClient.getInstance(refundRequest.getPartnerConfiguration());
    }
}
