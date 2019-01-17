package com.payline.payment.oney.service;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.service.impl.PaymentWithRedirectionServiceImpl;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.net.URISyntaxException;

import static com.payline.payment.oney.utils.TestUtils.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentWithRedirectionServiceTest {

    @InjectMocks
    public PaymentWithRedirectionServiceImpl service;

    @Spy
    OneyHttpClient httpClient;

    @BeforeAll
    public void setup() {
        service = new PaymentWithRedirectionServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void confirmPaymentTest() throws Exception {
        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(createCompleteRedirectionPaymentBuilder())
                .build();

        PaymentResponse response = service.validatePayment(paymentRequest);

        if (response.getClass() == PaymentResponseSuccess.class) {
            PaymentResponseSuccess success = (PaymentResponseSuccess) response;
            Assertions.assertEquals("200", success.getStatusCode());
            Assertions.assertEquals("Transaction is completed", success.getMessage().getMessage());
            Assertions.assertNotNull(success.getTransactionAdditionalData());
        }

    }

    @Test
    public void confirmPaymentTestInvalidData() throws Exception {
        StringResponse responseMocked = createStringResponse(400, "Bad Request", "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"Merchant_request_id\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}]}}");
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(createCompleteRedirectionPaymentBuilder())
                .build();

        PaymentResponse response = service.validatePayment(paymentRequest);
        PaymentResponseFailure fail = (PaymentResponseFailure) response;
        Assertions.assertEquals("400", fail.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, fail.getFailureCause());
    }


    @Test
    public void confirmPaymentTestNotFound() throws Exception {
        StringResponse responseMocked = createStringResponse(404, "Not Found", "{\"statusCode\": 404, \"message\": \"Resource not found\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(createCompleteRedirectionPaymentBuilder())
                .build();

        PaymentResponseFailure response = (PaymentResponseFailure) service.validatePayment(paymentRequest);
        Assertions.assertEquals("404", response.getErrorCode());
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, response.getFailureCause());


    }

    @Test
    public void finalizeRedirectionPaymentTestOK() throws IOException, URISyntaxException {
        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assertions.assertEquals(PaymentResponseSuccess.class, response.getClass());
        PaymentResponseSuccess success = (PaymentResponseSuccess) response;
        Assertions.assertNotNull(success.getPartnerTransactionId());
        Assertions.assertNotNull(success.getMessage());
        Assertions.assertNotNull(success.getStatusCode());
        Assertions.assertNotNull(success.getTransactionDetails());


    }

    @Test
    public void finalizeRedirectionPaymentTestKO() throws IOException, URISyntaxException {
        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"ymDHJ7HBRe49whKjH1HDtA==\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assertions.assertEquals(PaymentResponseFailure.class, response.getClass());
        PaymentResponseFailure failure = (PaymentResponseFailure) response;
        Assertions.assertNotNull(failure.getPartnerTransactionId());
        Assertions.assertNotNull(failure.getFailureCause());
        Assertions.assertEquals(FailureCause.REFUSED, failure.getFailureCause());

    }

    @Test
    public void handleSessionExpiredTestFunded() throws IOException, URISyntaxException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);

    }

    @Test
    public void handleSessionExpiredTestOnHold() throws IOException, URISyntaxException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"Zfxsl1nYU+7gI2vAD7S+JSO1EkNNk4gaIQcX++gJrX7NfjZ417t0L7ruzUCqFyxIVQWywc2FqrUK6J4kU5EPh0ksAzV6KmKWDolDoGte7uENMlMzcTriutnu5d/fJEf1\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseOnHold.class);

    }

    @Test
    public void handleSessionExpiredTestRefused() throws IOException, URISyntaxException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"ymDHJ7HBRe49whKjH1HDtA==\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseFailure.class);

    }

    @Test
    public void handleSessionExpiredTestFavorable() throws IOException, URISyntaxException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");
        Mockito.doReturn(responseMocked).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());
        //Mock appel post dans PaymentWithRedirection.validate()
        StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked2).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);

    }
}
