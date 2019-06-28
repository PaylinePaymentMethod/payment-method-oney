package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.response.OneyNotificationResponse;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.FailureTransactionStatus;
import com.payline.pmapi.bean.common.SuccessTransactionStatus;
import com.payline.pmapi.bean.common.TransactionStatus;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.service.NotificationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
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
        String transactionId = "UNKNOWN";
        String partnerTransactionId = "UNKNOWN";
        TransactionStateChangedResponse.Action action = TransactionStateChangedResponse.Action.AUTHOR_AND_CAPTURE;
        TransactionStatus status;

        // get the body of the request
        try (BufferedReader br = new BufferedReader(new InputStreamReader(notificationRequest.getContent(), "UTF-8"))) {
            String bodyResponse = br.lines().collect(Collectors.joining(System.lineSeparator()));
            final String key = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, OneyConstants.PARTNER_CHIFFREMENT_KEY);

            // extract all needed data
            OneyNotificationResponse oneyResponse = OneyNotificationResponse.createTransactionStatusResponseFromJson(bodyResponse, key);
            transactionId = oneyResponse.getPspContext();
            partnerTransactionId = oneyResponse.getPurchase().getExternalReference();


            // check the payment status
            String paymentStatus = oneyResponse.getPurchase().getStatusCode();
            switch (paymentStatus) {
                case "FUNDED":
                    status = new SuccessTransactionStatus();
                    break;
                case "FAVORABLE":
                    // if captureNow => does to comfirm call
                    if (PluginUtils.isCaptureNow(oneyResponse.getMerchantContext())) {
                        // confirm the request
                        OneyConfirmRequest confirmRequest = new OneyConfirmRequest.Builder(notificationRequest, oneyResponse)
                                .build();
                        StringResponse confirmResponse = httpClient.initiateConfirmationPayment(confirmRequest, notificationRequest.getEnvironment().isSandbox());


                        // check the confirmation response
                        if (confirmResponse == null || confirmResponse.getContent() == null || confirmResponse.getCode() != HTTP_OK) {
                            // unable to read the response
                            LOGGER.error("Unable to read the confirmation response");
                            status = new FailureTransactionStatus(FailureCause.COMMUNICATION_ERROR);
                            break;
                        }

                        TransactionStatusResponse confirmTransactionResponse = createTransactionStatusResponseFromJson(confirmResponse.getContent(), key);
                        if (confirmTransactionResponse == null || confirmTransactionResponse.getStatusPurchase() == null) {
                            // unable to read the payment status
                            LOGGER.error("Unable to read the comfiration response transaction status");
                            status = new FailureTransactionStatus(FailureCause.COMMUNICATION_ERROR);
                            break;
                        }

                        // check the confirmation response status
                        String confirmStatus = confirmTransactionResponse.getStatusPurchase().getStatusCode();
                        if ("FUNDED".equals(confirmStatus)) {
                            // success
                            status = new SuccessTransactionStatus();
                        } else {
                            status = new FailureTransactionStatus(FailureCause.REFUSED);
                        }
                    }
                    status = new SuccessTransactionStatus();

                    break;
                case "REFUSED":
                    status = new FailureTransactionStatus(FailureCause.REFUSED);
                    break;
                case "ABORTED":
                    status = new FailureTransactionStatus(FailureCause.CANCEL);
                    break;
                default:
                    // Ignore the notification, with a 204 HTTP status code
                    return IgnoreNotificationResponse.IgnoreNotificationResponseBuilder.aIgnoreNotificationResponseBuilder()
                            .withHttpStatus(204)
                            .build();
            }


        } catch (IOException e) {
            LOGGER.error("Unable to read the notification request's body");
            status = new FailureTransactionStatus(FailureCause.COMMUNICATION_ERROR);
        } catch (PluginTechnicalException e) {
            LOGGER.error("Unable to get infomation needed to know the transaction status");
            status = new FailureTransactionStatus(e.getFailureCause());
        }

        return TransactionStateChangedResponse.TransactionStateChangedResponseBuilder.aTransactionStateChangedResponse()
                .withTransactionId(transactionId)
                .withPartnerTransactionId(partnerTransactionId)
                .withStatusDate(new Date())
                .withAction(action)
                .withTransactionStatus(status)
                .build();
    }

    @Override
    public void notifyTransactionStatus(NotifyTransactionStatusRequest notifyTransactionStatusRequest) {
        //ras.

    }
}
