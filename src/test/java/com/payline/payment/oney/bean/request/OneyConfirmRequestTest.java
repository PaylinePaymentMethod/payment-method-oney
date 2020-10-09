package com.payline.payment.oney.bean.request;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.TestUtils.createCompleteRedirectionPaymentBuilder;

class OneyConfirmRequestTest {

    private OneyConfirmRequest paymentRequest;

    @Test
    void buildOneyConfirmRequest() throws Exception {

        paymentRequest = new OneyConfirmRequest.Builder(createCompleteRedirectionPaymentBuilder())
                .build();

        Assertions.assertFalse(paymentRequest.toString().contains("merchant_guid"));
        Assertions.assertFalse(paymentRequest.toString().contains("psp_guid"));
        Assertions.assertTrue(paymentRequest.toString().contains("language_code"));
        Assertions.assertTrue(paymentRequest.toString().contains("merchant_request_id"));
        Assertions.assertTrue(paymentRequest.toString().contains("payment"));
        Assertions.assertTrue(paymentRequest.toString().contains("payment_amount"));
    }

}
