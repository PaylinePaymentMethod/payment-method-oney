package com.payline.payment.oney.response;

import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.utils.http.StringResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

public class TransactionStatusResponseTest {

    private String key = "66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=";

    @Test
    public void transactionStatusTest() throws DecryptException {
        String responseDecrypted = "{\"purchase\": { \"status_code\": \"PENDING\", \"status_label\": \"Waiting for customer validation\" }}";

        //Cas reponse dechiffree
        TransactionStatusResponse status1 = createTransactionStatusResponseFromJson(responseDecrypted, key);
        Assertions.assertEquals("PENDING", status1.getStatusPurchase().getStatusCode());
        Assertions.assertEquals("Waiting for customer validation", status1.getStatusPurchase().getStatusLabel());

    }

    @Test
    public void transactionStatusEncryptedTest() throws DecryptException {
        StringResponse encryptedResponse = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");

        //Cas reponse dechiffre
        TransactionStatusResponse status1 = createTransactionStatusResponseFromJson(encryptedResponse.getContent(), key);
        Assertions.assertEquals("FUNDED", status1.getStatusPurchase().getStatusCode());
        Assertions.assertEquals("Transaction is completed", status1.getStatusPurchase().getStatusLabel());


    }


}
