package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;

public class PurchaseNotification extends PurchaseStatus{
    @SerializedName("external_reference")
    private String externalReference;

    public PurchaseNotification(String statusLabel, StatusCode statusCode, String reasonCode, String reasonLabel) {
        super(statusLabel, statusCode, reasonCode, reasonLabel);
    }

    public String getExternalReference() {
        return externalReference;
    }
}
