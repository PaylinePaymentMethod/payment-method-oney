package com.payline.payment.oney.bean.response;

import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.exception.MalformedJsonException;
import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.http.StringResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.PENDING;
import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;


public class TransactionStatusResponseTest extends OneyConfigBean {


    private String key = "66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=";


    @Test
    public void transactionStatusNotEncrypted() throws DecryptException, MalformedJsonException {
        StringResponse encryptedResponse = createStringResponse(400, "OK", "{\"purchase\":{\"status_code\":\"PENDING\",\"status_label\":\"Waiting for customer validation\"}}");


        mockCorrectlyConfigPropertiesEnum(false);
        //Cas reponse dechiffre
        TransactionStatusResponse status1 = createTransactionStatusResponseFromJson(encryptedResponse.getContent(), null);
        Assertions.assertEquals(PENDING, status1.getStatusPurchase().getStatusCode());
        Assertions.assertEquals("Waiting for customer validation", status1.getStatusPurchase().getStatusLabel());


    }

    @Test
    public void transactionStatusEncrypted() throws DecryptException, MalformedJsonException {
        StringResponse encryptedResponse = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul3aakmok0anPtpBvW1vZ3e83c7evaIMgKsuqlJpPjg407AoMkFm94736cZcnpC81qiX4V8n9IxMD1E50QBAOkMZ1S8Pf90kxhXSDe3wt4J13\"}");


        mockCorrectlyConfigPropertiesEnum(true);
        //Cas reponse chiffre
        TransactionStatusResponse status1 = createTransactionStatusResponseFromJson(encryptedResponse.getContent(), key);
        Assertions.assertEquals(PENDING, status1.getStatusPurchase().getStatusCode());
        Assertions.assertEquals("Waiting for customer validation", status1.getStatusPurchase().getStatusLabel());


    }

}
