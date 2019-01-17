package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;

public class OneyError extends OneyBean {

    @SerializedName("field")
    private String field;
    @SerializedName("error_code")
    private String errorCode = "";
    @SerializedName("error_label")
    private String errorLabel;

    private OneyError error;


    public String getField() {
        return field;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorLabel() {
        return errorLabel;
    }

    public OneyError getError() {
        return error;
    }


}
