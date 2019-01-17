package com.payline.payment.oney.request;

import com.payline.payment.oney.bean.request.OneyRefundRequest;
import com.payline.payment.oney.utils.PluginUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;

import static com.payline.payment.oney.utils.TestUtils.CONFIRM_AMOUNT;
import static com.payline.payment.oney.utils.TestUtils.createDefaultRefundRequest;

public class OneyRefundRequestTest {

    @Test
    public void createRefundRequestOK() {
        OneyRefundRequest request = OneyRefundRequest.Builder.aOneyRefundRequest()
                .fromRefundRequest(createDefaultRefundRequest(),true)
                .build();

        Assertions.assertNotNull(request);
        Assertions.assertNotNull(request.getPurchaseReference());
        Assertions.assertNotNull(request.getMerchantGuid());
        Assertions.assertTrue(request.toString().contains("merchant_request_id"));
        Assertions.assertTrue(request.toString().contains("purchase"));

        Float paymentAmountConverted  = PluginUtils.createFloatAmount(new BigInteger(CONFIRM_AMOUNT), Currency.getInstance("EUR"));
        Assertions.assertEquals(paymentAmountConverted, request.getPurchase().getAmount(),0.01);
    }

    @Test
    public void createRefundRequestKO() {
        //todo

    }

}
