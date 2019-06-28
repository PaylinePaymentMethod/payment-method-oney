package com.payline.payment.oney.exception;

import com.payline.pmapi.bean.common.FailureCause;

public class MalformedJsonException extends PluginTechnicalException {

    private static final String ERROR_CODE = "Unable to parse JSON content";

    /**
     * Exception constructor
     * @param exception Initial exception thrown
     */
    public MalformedJsonException(Exception exception) {
        super(exception, ERROR_CODE);
    }

    /**
     * Exception constructor from a message.
     * The first argument passed to super() will be logged, the second will be returned to the PM-API.
     * @param message Error message
     */
    public MalformedJsonException( String message ) {
        super( message, message );
    }

    @Override
    public FailureCause getFailureCause() {
        return FailureCause.COMMUNICATION_ERROR;
    }
}
