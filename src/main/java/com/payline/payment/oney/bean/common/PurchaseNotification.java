package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;

public class PurchaseNotification {
    @SerializedName("external_reference")
    private String externalReference;

    @SerializedName("status_label")
    private String statusLabel;
    @SerializedName("status_code")
    private String statusCode;
    @SerializedName("reason_code")
    private String reasonCode;
    @SerializedName("reason_label")
    private String reasonLabel;


    public String getExternalReference() {
        return externalReference;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public String getReasonLabel() {
        return reasonLabel;
    }
}
