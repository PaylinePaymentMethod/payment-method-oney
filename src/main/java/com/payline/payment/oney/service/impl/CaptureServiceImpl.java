package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.capture.response.CaptureResponse;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseFailure;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseSuccess;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.service.CaptureService;

import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.FAVORABLE;
import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.FUNDED;
import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;

public class CaptureServiceImpl implements CaptureService {
    private static final String ERROR_STATUS = "TRANSACTION STATUS NOT FAVORABLE:";

    @Override
    public CaptureResponse captureRequest(CaptureRequest captureRequest) {
        String transactionId = captureRequest.getPartnerTransactionId();
        boolean isSandbox = captureRequest.getEnvironment().isSandbox();
        try {

            // call the get status request
            OneyTransactionStatusRequest oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .fromCaptureRequest(captureRequest).build();
            final OneyHttpClient httpClient = getNewHttpClientInstance(captureRequest);
            StringResponse oneyResponse = httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest, isSandbox);

            // check the result
            if (oneyResponse == null || oneyResponse.getContent() == null || oneyResponse.getCode() != HTTP_OK) {
                return createFailure(transactionId, "Unable to get transaction status", FailureCause.COMMUNICATION_ERROR);

            } else {
                // get the payment status
                TransactionStatusResponse statusResponse = createTransactionStatusResponseFromJson(oneyResponse.getContent(), oneyTransactionStatusRequest.getEncryptKey());
                if (statusResponse == null || statusResponse.getStatusPurchase() == null) {
                    return createFailure(transactionId, "Unable to get transaction status", FailureCause.COMMUNICATION_ERROR);

                } else {
                    // check the status
                    PurchaseStatus.StatusCode getStatus = statusResponse.getStatusPurchase().getStatusCode();
                    if (FAVORABLE.equals(getStatus)) {
                        // confirm the transaction
                        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(captureRequest).build();
                        StringResponse confirmResponse = httpClient.initiateConfirmationPayment(confirmRequest, isSandbox);

                        // check the confirmation response
                        if (confirmResponse == null || confirmResponse.getContent() == null || confirmResponse.getCode() != HTTP_OK) {
                            return createFailure(transactionId, "Unable to confirm transaction", FailureCause.COMMUNICATION_ERROR);
                        }
                        TransactionStatusResponse confirmTransactionResponse = createTransactionStatusResponseFromJson(confirmResponse.getContent(), oneyTransactionStatusRequest.getEncryptKey());

                        if (confirmTransactionResponse == null || confirmTransactionResponse.getStatusPurchase() == null) {
                            return createFailure(transactionId, "Unable to confirm transaction", FailureCause.COMMUNICATION_ERROR);
                        }

                        // check the confirmation response status
                        PurchaseStatus.StatusCode  confirmStatus = confirmTransactionResponse.getStatusPurchase().getStatusCode();
                        if (FUNDED.equals(confirmStatus)) {
                            return CaptureResponseSuccess.CaptureResponseSuccessBuilder.aCaptureResponseSuccess()
                                    .withPartnerTransactionId(captureRequest.getPartnerTransactionId())
                                    .withStatusCode(confirmStatus.name())
                                    .build();
                        } else {
                            return createFailure(transactionId, ERROR_STATUS + confirmStatus, FailureCause.REFUSED);
                        }
                    } else {
                        return createFailure(transactionId, ERROR_STATUS + getStatus, FailureCause.REFUSED);
                    }
                }
            }

        } catch (PluginTechnicalException e) {
            return createFailure(transactionId, e.getTruncatedErrorCodeOrLabel(), e.getFailureCause());
        }
    }

    protected OneyHttpClient getNewHttpClientInstance(final CaptureRequest captureRequest) {
        return OneyHttpClient.getInstance(captureRequest.getPartnerConfiguration());
    }

    @Override
    public boolean canMultiple() {
        return false;
    }

    @Override
    public boolean canPartial() {
        return false;
    }

    CaptureResponseFailure createFailure(String transactionId, String errorCode, FailureCause cause) {
        return CaptureResponseFailure.CaptureResponseFailureBuilder.aCaptureResponseFailure()
                .withPartnerTransactionId(transactionId)
                .withErrorCode(errorCode)
                .withFailureCause(cause)
                .build();

    }
}
