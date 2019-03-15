package com.payline.payment.oney.bean.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OneyTransactionStatusRequestTest {

    @Test
    public void testBuildRequestOK() throws Exception {
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
