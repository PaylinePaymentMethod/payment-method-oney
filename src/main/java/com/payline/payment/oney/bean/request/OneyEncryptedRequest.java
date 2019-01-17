package com.payline.payment.oney.bean.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.exception.DecryptException;

public class OneyEncryptedRequest extends OneyBean {
    @SerializedName("merchant_guid")
    protected String merchantGuid;
    @SerializedName("psp_guid")
    protected String pspGuid;

    @Expose
    @SerializedName("encrypted_message")
    protected String encryptedMessage;

    public String getMerchantGuid() {
        return merchantGuid;
    }

    public String getPspGuid() {
        return pspGuid;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(String encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }

    public static OneyEncryptedRequest fromOneyPaymentRequest(OneyPaymentRequest request) throws DecryptException {
        OneyEncryptedRequest encryptedRequest = new OneyEncryptedRequest();
        encryptedRequest.encryptedMessage = OneyRequest.encryptMessage(request.toString(), request.getEncryptKey());
        encryptedRequest.pspGuid = request.pspGuid;
        encryptedRequest.merchantGuid = request.merchantGuid;

        return encryptedRequest;
    }

    public static OneyEncryptedRequest fromOneyConfirmRequest(OneyConfirmRequest request) throws DecryptException {
        OneyEncryptedRequest encryptedRequest = new OneyEncryptedRequest();
        encryptedRequest.encryptedMessage = OneyRequest.encryptMessage(request.toString(), request.getEncryptKey());

        return encryptedRequest;
    }

    public static OneyEncryptedRequest fromOneyRefundRequest(OneyRefundRequest request) throws DecryptException {
        OneyEncryptedRequest encryptedRequest = new OneyEncryptedRequest();
        encryptedRequest.encryptedMessage = OneyRequest.encryptMessage(request.toString(), request.getEncryptKey());
        encryptedRequest.pspGuid = request.pspGuid;
        encryptedRequest.merchantGuid = request.merchantGuid;

        return encryptedRequest;
    }
}
