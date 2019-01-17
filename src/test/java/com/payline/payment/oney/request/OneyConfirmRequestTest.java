package com.payline.payment.oney.request;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.TestUtils.createCompleteRedirectionPaymentBuilder;

public class OneyConfirmRequestTest {

    private OneyConfirmRequest paymentRequest;

    @Test
    public void buildOneyConfirmRequest() {

        paymentRequest = new OneyConfirmRequest.Builder(createCompleteRedirectionPaymentBuilder())
                .build();

        Assertions.assertFalse(paymentRequest.toString().contains("merchant_guid"));
        Assertions.assertFalse(paymentRequest.toString().contains("psp_guid"));
        Assertions.assertTrue(paymentRequest.toString().contains("language_code"));
        Assertions.assertTrue(paymentRequest.toString().contains("merchant_request_id"));
        Assertions.assertTrue(paymentRequest.toString().contains("payment"));
    }

}
