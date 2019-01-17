package com.payline.payment.oney.bean.response;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.bean.request.OneyRequest;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.utils.chiffrement.OneyCrypto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class OneyResponse extends OneyBean {

    private static final Logger LOGGER = LogManager.getLogger(OneyRequest.class);

    @SerializedName("encrypted_message")
    protected String encryptedMessage;


    public String getEncryptedMessage() {
        return encryptedMessage;
    }


    /**
     * Decrypt a request  message
     *
     * @param toDecrypt, the String to decrypt
     * @param key,       String,  the chiffrement key
     * @return
     */
    public static String decryptMessage(String toDecrypt, String key) throws DecryptException {
        if (toDecrypt == null || toDecrypt.equals("")) {
            LOGGER.error("Message to decrypt is empty");
            return "";
        }

        OneyCrypto crypto = new OneyCrypto(key);
        return crypto.decrypt(toDecrypt);

    }
}
