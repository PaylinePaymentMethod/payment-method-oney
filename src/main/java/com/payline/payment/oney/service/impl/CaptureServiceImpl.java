package com.payline.payment.oney.service.impl;

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
import com.payline.pmapi.logger.LogManager;
import com.payline.pmapi.service.CaptureService;
import org.apache.logging.log4j.Logger;

import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.OneyConstants.HTTP_OK;

public class CaptureServiceImpl implements CaptureService {
    private final String ERROR_STATUS = "TRANSACTION STATUS NOT FAVORABLE:";
    private OneyHttpClient httpClient;
    private static final Logger LOGGER = LogManager.getLogger(CaptureServiceImpl.class);

    public CaptureServiceImpl() {
        httpClient = OneyHttpClient.getInstance();
    }

    @Override
    public CaptureResponse captureRequest(CaptureRequest captureRequest) {
        String transactionId = captureRequest.getPartnerTransactionId();
        boolean isSandbox = captureRequest.getEnvironment().isSandbox();
        try {

            // call the get status request
            OneyTransactionStatusRequest oneyTransactionStatusRequest = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .fromCaptureRequest(captureRequest).build();
            StringResponse oneyResponse = this.httpClient.initiateGetTransactionStatus(oneyTransactionStatusRequest, isSandbox);

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
                    String getStatus = statusResponse.getStatusPurchase().getStatusCode();
                    if ("FAVORABLE".equals(getStatus)) {
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
                        String confirmStatus = confirmTransactionResponse.getStatusPurchase().getStatusCode();
                        if ("FUNDED".equals(confirmStatus)) {
                            return CaptureResponseSuccess.CaptureResponseSuccessBuilder.aCaptureResponseSuccess()
                                    .withPartnerTransactionId(captureRequest.getPartnerTransactionId())
                                    .withStatusCode(confirmStatus)
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
