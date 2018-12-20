package com.payline.payment.oney.service.impl.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.exception.DecryptException;

public class TransactionStatusResponse extends OneyResponse {

    @SerializedName("language_code")
    private String languageCode;

    @SerializedName("purchase")
    private PurchaseStatus statusPurchase;


    public PurchaseStatus getStatusPurchase() {
        return statusPurchase;
    }

    public String getLanguageCode() {
        return languageCode;
    }


    public static TransactionStatusResponse createTransactionStatusResponseFromJson(String json,String encryptKey) throws DecryptException {
        Gson parser = new Gson();

        TransactionStatusResponse transactionStatusResponse = parser.fromJson(json, TransactionStatusResponse.class);

        //Cas reponse est chiffree : on dechiffre la reponse afin de recuperer le statut de la transaction
        if (transactionStatusResponse.getStatusPurchase() == null) {
            String decryptedMessage = OneyResponse.decryptMessage(transactionStatusResponse.getEncryptedMessage(), encryptKey);

            return parser.fromJson(decryptedMessage, TransactionStatusResponse.class);

        }
        //Sinon on renvoie la reponse parsee
        else {
            return transactionStatusResponse;
        }
    }


}
