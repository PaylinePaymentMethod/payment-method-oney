package com.payline.payment.oney.bean.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.exception.DecryptException;

public class OneySuccessPaymentResponse extends OneyResponse {

    @SerializedName("returned_url")
    private String returnedUrl;


    public String getReturnedUrl() {
        return returnedUrl;
    }

    public void setReturnedUrl(String url) {
        this.returnedUrl = url;
    }


    public static OneySuccessPaymentResponse paymentSuccessResponseFromJson(String json, String encryptKey) throws DecryptException {
        Gson parser = new Gson();
        OneySuccessPaymentResponse paymentSuccessResponse = parser.fromJson(json, OneySuccessPaymentResponse.class);

        //Cas reponse est chiffree : on dechiffre la reponse afin de recuperer l'url a renvoyer
        if (paymentSuccessResponse.getReturnedUrl() == null) {
            String decryptedMessage = OneyResponse.decryptMessage(paymentSuccessResponse.getEncryptedMessage(), encryptKey);
            paymentSuccessResponse.setReturnedUrl(parser.fromJson(decryptedMessage, OneySuccessPaymentResponse.class).getReturnedUrl());
        }
        return paymentSuccessResponse;
    }

}
