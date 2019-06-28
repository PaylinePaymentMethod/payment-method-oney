package com.payline.payment.oney.utils;

import com.payline.payment.oney.bean.common.OneyError;
import com.payline.payment.oney.bean.response.OneyFailureResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

public class OneyErrorHandler {

    private static final Logger LOGGER = LogManager.getLogger(OneyErrorHandler.class);

    private OneyErrorHandler() {
        super();
    }

    public static PaymentResponseFailure getPaymentResponseFailure(final FailureCause failureCause, String externalReference, String errorCode) {
        return PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(failureCause)
                .withPartnerTransactionId(externalReference)
                .withErrorCode(errorCode)
                .build();
    }

    public static RefundResponseFailure geRefundResponseFailure(final FailureCause failureCause, String externalReference, String errorCode) {
        return RefundResponseFailure.RefundResponseFailureBuilder.aRefundResponseFailure()
                .withFailureCause(failureCause)
                .withPartnerTransactionId(externalReference)
                .withErrorCode(errorCode)
                .build();
    }

    public static FailureCause handleOneyFailureResponse(OneyFailureResponse failureResponse) {

        int failureCode = failureResponse.getCode();
        FailureCause paylineCause;
        switch (failureCode) {
            case 401:
            case 403:
            case 500:
                paylineCause = FailureCause.REFUSED;
                break;
            case 404:
            case 408:
            case 429:
            case 503:
            case 504:
                paylineCause = FailureCause.COMMUNICATION_ERROR;
                break;
            case 400:
                paylineCause = handleOneyFailureResponseFromCause(failureResponse);
                break;
            case 409:
                paylineCause = FailureCause.INVALID_DATA;
                break;
            default:
                paylineCause = FailureCause.PARTNER_UNKNOWN_ERROR;
        }
        LOGGER.warn("failure : code oney {}, payline correspondance {}", failureCode, paylineCause);
        return paylineCause;
    }


    public static FailureCause handleOneyFailureResponseFromCause(OneyFailureResponse failureResponse) {
        String failureCause = null;

        //Si le tableau contient plusieurs erreurs on récupère la première. toutes les autres seront loggués
        if (failureResponse.getPaymentErrorContent() != null && failureResponse.getPaymentErrorContent().getErrorList().get(0) != null) {
            OneyError oneyError = failureResponse.getPaymentErrorContent().getErrorList().get(0);
            if (oneyError != null) {
                if (oneyError.getError() == null) {
                    failureCause = oneyError.getErrorCode();
                } else {
                    failureCause = oneyError.getError().getErrorCode();
                }
            }

            LOGGER.warn(failureCause);
        }

        if (failureCause == null || failureCause.isEmpty()) {
            LOGGER.warn("Oney error not parsable");
            return FailureCause.PARTNER_UNKNOWN_ERROR;
        }

        //contiendra le error_code de la 1ere erreur
        FailureCause paylineCause;
        switch (failureCause) {
            case "ERR_01":
            case "ERR_05":
                paylineCause = FailureCause.REFUSED;
                break;
            case "ERR_02":
            case "ERR_03":
                paylineCause = FailureCause.INVALID_FIELD_FORMAT;
                break;
            case "ERR_04":
                paylineCause = FailureCause.INVALID_DATA;
                break;

            default:
                paylineCause = FailureCause.INVALID_DATA;
        }

        return paylineCause;
    }

}


