package com.payline.payment.oney.request;

import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OneyTransactionStatusRequestTest {

    @Test
    public void testBuildRequestKO_emptyCallParameters() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("guid")
                    .withPspGuid("pspG")
                    .withPurchaseReference("maCommande")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .withCallParameters(new HashMap<>())
                    .build();
        });
    }

    @Test
    public void testBuildRequestKO_nullCallParameters() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("guid")
                    .withPspGuid("pspG")
                    .withPurchaseReference("maCommande")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .build();
        });
    }

    @Test
    public void testBuildRequestKO_EncryptKey() {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("guid")
                    .withPspGuid("pspG")
                    .withPurchaseReference("maCommande")
                    .withCallParameters(map)
                    .build();
        });
    }

    @Test
    public void testBuildRequestKO_PurchaseReference() {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("guid")
                    .withPspGuid("pspG")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .withCallParameters(map)
                    .build();
        });
    }

    @Test
    public void testBuildRequestKO_PspGuid() {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("guid")
                    .withPurchaseReference("maCommande")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .withCallParameters(map)
                    .build();
        });
    }

    @Test
    public void testBuildRequestKO_MerchantGuid() {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withPspGuid("pspG")
                    .withPurchaseReference("maCommande")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .withCallParameters(map)
                    .build();
        });
    }


    @Test
    public void testBuildRequestOK() {
        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("guid")
                .withPspGuid("pspG")
                .withPurchaseReference("maCommande")
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withCallParameters(map)
                .build();

        Assertions.assertEquals("FR", request.getLanguageCode());
        Assertions.assertEquals("guid", request.getMerchantGuid());
        Assertions.assertEquals("maCommande", request.getPurchaseReference());
    }
}
