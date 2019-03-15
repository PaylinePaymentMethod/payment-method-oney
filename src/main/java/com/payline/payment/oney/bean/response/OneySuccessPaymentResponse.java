package com.payline.payment.oney.bean.response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.exception.MalformedResponseException;
import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

import static com.payline.payment.oney.utils.OneyConstants.CHIFFREMENT_IS_ACTIVE;

public class OneySuccessPaymentResponse extends OneyResponse {

    private static final Logger LOGGER = LogManager.getLogger(OneySuccessPaymentResponse.class);

    @SerializedName("returned_url")
    private String returnedUrl;


    public String getReturnedUrl() {
        return returnedUrl;
    }

    public URL getReturnedUrlAsUrl() throws HttpCallException {

        try {
            return new URL(returnedUrl);
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new HttpCallException(e, "OneySuccessPaymentResponse.MalformedURLException");
        }
    }

    public void setReturnedUrl(String url) {
        this.returnedUrl = url;
    }


    public static OneySuccessPaymentResponse paymentSuccessResponseFromJson(String json, String encryptKey)
            throws DecryptException, MalformedResponseException {
        Gson parser = new Gson();
        OneySuccessPaymentResponse paymentSuccessResponse;
        try {
            paymentSuccessResponse = parser.fromJson(json, OneySuccessPaymentResponse.class);
        }
        catch( JsonSyntaxException e){
            LOGGER.error("Unable to parse JSON content", e);
            throw new MalformedResponseException( e );
        }

        //Cas reponse est chiffree : on dechiffre la reponse afin de recuperer l'url a renvoyer
        if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            String decryptedMessage = OneyResponse.decryptMessage(paymentSuccessResponse.getEncryptedMessage(), encryptKey);
            paymentSuccessResponse.setReturnedUrl(parser.fromJson(decryptedMessage, OneySuccessPaymentResponse.class).getReturnedUrl());
        }
        return paymentSuccessResponse;
    }

}
