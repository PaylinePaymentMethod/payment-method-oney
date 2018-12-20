package com.payline.payment.oney.service.impl.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.utils.chiffrement.OneyCrypto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class OneyRequest extends OneyBean {
    //Construit une requête Oney

    private static final Logger LOGGER = LogManager.getLogger(OneyRequest.class);
    @SerializedName("merchant_guid")
    protected String merchantGuid;
    @SerializedName("psp_guid")
    protected String pspGuid;
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

    /**
     * Encrypt a request  message
     *
     * @param toEncrypt, the String to encrypt
     * @param key,       String,  the chiffrement key
     * @return
     */
    public static String encryptMessage(String toEncrypt, String key) throws DecryptException {
        if (toEncrypt == null) {
            LOGGER.info("Message to encrypt is empty");
            toEncrypt = "";
        }

        OneyCrypto crypto = new OneyCrypto(key);
        return crypto.encrypt(toEncrypt);
    }


}
