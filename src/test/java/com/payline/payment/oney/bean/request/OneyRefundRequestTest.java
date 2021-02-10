package com.payline.payment.oney.bean.request;

import com.payline.payment.oney.utils.PluginUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;

import static com.payline.payment.oney.utils.TestUtils.CONFIRM_AMOUNT;
import static com.payline.payment.oney.utils.TestUtils.createDefaultRefundRequest;

class OneyRefundRequestTest {

    @Test
    public void createRefundRequestOK() throws Exception {
        OneyRefundRequest request = OneyRefundRequest.Builder.aOneyRefundRequest()
                .fromRefundRequest(createDefaultRefundRequest(), true)
                .build();

        Assertions.assertNotNull(request);
        Assertions.assertNotNull(request.getPurchaseReference());
        Assertions.assertNotNull(request.getMerchantGuid());
        Assertions.assertTrue(request.toString().contains("merchant_request_id"));
        Assertions.assertTrue(request.toString().contains("purchase"));
        Assertions.assertTrue(request.toString().contains("cancellation_reason_code"));

        Float paymentAmountConverted = PluginUtils.createFloatAmount(new BigInteger(CONFIRM_AMOUNT), Currency.getInstance("EUR"));
        Assertions.assertEquals(paymentAmountConverted, request.getPurchase().getAmount(), 0.01);
    }

}
