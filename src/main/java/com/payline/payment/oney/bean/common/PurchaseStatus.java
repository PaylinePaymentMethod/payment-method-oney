package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;

public class PurchaseStatus {

    @SerializedName("status_label")
    private String statusLabel;
    @SerializedName("status_code")
    private StatusCode statusCode;
    @SerializedName("reason_code")
    private String reasonCode;
    @SerializedName("reason_label")
    private String reasonLabel;

    public String getStatusLabel() {
        return statusLabel;
    }

    public StatusCode getStatusCode() {
        return statusCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getReasonLabel() {
        return reasonLabel;
    }


    public PurchaseStatus(String statusLabel, StatusCode statusCode, String reasonCode, String reasonLabel) {
        this.statusLabel = statusLabel;
        this.statusCode = statusCode;
        this.reasonCode = reasonCode;
        this.reasonLabel = reasonLabel;
    }


    public enum StatusCode{
        PENDING,REFUSED,ABORTED,FAVORABLE,FUNDED,CANCELLED,TO_BE_FUNDED
    }


}
