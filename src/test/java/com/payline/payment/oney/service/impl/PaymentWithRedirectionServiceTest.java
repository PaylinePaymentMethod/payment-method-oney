package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseActiveWaiting;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.payline.payment.oney.utils.TestUtils.*;
import static org.mockito.ArgumentMatchers.anyBoolean;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentWithRedirectionServiceTest extends OneyConfigBean {

    @InjectMocks
    public PaymentWithRedirectionServiceImpl service;

    @Mock
    OneyHttpClient httpClient;

    @BeforeEach
    public void setup() {
        service = new PaymentWithRedirectionServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void confirmPaymentTest() throws Exception {
        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        PaymentResponse response = service.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference());

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
        Mockito.doReturn(responseMocked).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        PaymentResponse response = service.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference());
        PaymentResponseFailure fail = (PaymentResponseFailure) response;
        Assertions.assertEquals("400 - ERR_04 - Merchant_request_id", fail.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, fail.getFailureCause());
    }


    @Test
    public void confirmPaymentTestNotFound() throws Exception {
        StringResponse responseMocked = createStringResponse(404, "Not Found", "{\"statusCode\": 404, \"message\": \"Resource not found\"}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        PaymentResponseFailure response = (PaymentResponseFailure) service.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference());
        Assertions.assertEquals("404", response.getErrorCode());
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, response.getFailureCause());
    }

    @Test
    public void validatePayment_malformedConfirmResponseKO() throws PluginTechnicalException {
        // given a malformed HTTP response received to the confirm request
        StringResponse responseMocked = createStringResponse(404, "Bad request", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        // when calling the method validatePayment
        PaymentResponse response = service.validatePayment( paymentRequest, true, redirectionPaymentRequest.getOrder().getReference());

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue( response instanceof PaymentResponseFailure );
        Assertions.assertEquals( FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure)response).getFailureCause() );
    }

    @Test
    public void validatePayment_malformedConfirmResponseOK() throws PluginTechnicalException {
        // given a malformed HTTP response received to the confirm request
        StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        // when calling the method validatePayment
        PaymentResponse response = service.validatePayment( paymentRequest, true, redirectionPaymentRequest.getOrder().getReference());

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue( response instanceof PaymentResponseFailure );
        Assertions.assertEquals( FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure)response).getFailureCause() );
    }

    @Test
    public void finalizeRedirectionPaymentTest() {
        RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        PaymentResponse response = service.finalizeRedirectionPayment(redirectionPaymentRequest);

        Assertions.assertEquals(PaymentResponseActiveWaiting.class, response.getClass());
    }

    @Test
    public void handleSessionExpiredFundedEncrypted() throws PluginTechnicalException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(), anyBoolean() );
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);

    }

    @Test
    public void handleSessionExpiredFundedNotEncrypted() throws PluginTechnicalException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"Transaction is completed\"}}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(), anyBoolean() );
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);

    }


    @Test
    public void handleSessionExpiredOnHoldEncrypted() throws PluginTechnicalException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"Zfxsl1nYU+7gI2vAD7S+JSO1EkNNk4gaIQcX++gJrX7NfjZ417t0L7ruzUCqFyxIVQWywc2FqrUK6J4kU5EPh0ksAzV6KmKWDolDoGte7uENMlMzcTriutnu5d/fJEf1\"}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(), anyBoolean() );
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseOnHold.class);

    }

    @Test
    public void handleSessionExpiredOnHoldNotEncrypted() throws PluginTechnicalException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\": { \"status_code\": \"PENDING\", \"status_label\": \"Waiting for customer validation\" }}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(OneyTransactionStatusRequest.class), anyBoolean() );
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseOnHold.class);

    }


    @Test
    public void handleSessionExpiredTestRefused() throws PluginTechnicalException {
        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"ymDHJ7HBRe49whKjH1HDtA==\"}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(OneyTransactionStatusRequest.class), anyBoolean() );
        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseFailure.class);

    }

    @Test
    public void handleSessionExpiredFavorableEncrypted() throws PluginTechnicalException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(), anyBoolean() );
        StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        Mockito.doReturn(responseMocked2).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);

    }

    @Test
    public void     handleSessionExpiredFavorableNotEncrypted() throws PluginTechnicalException {

        StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Oney accepts the payment\"}}");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(), anyBoolean() );
        StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Oney accepts the payment\"}}");
        Mockito.doReturn(responseMocked2).when(httpClient).initiateConfirmationPayment( Mockito.any(OneyConfirmRequest.class), anyBoolean() );

        TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        PaymentResponse paymentResponse = service.handleSessionExpired(transactionStatusReq);
        Assertions.assertNotNull(paymentResponse);
        Assertions.assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);

    }

    @Test
    public void handleSessionExpired_malformedStatusResponseOK() throws PluginTechnicalException {
        // given a malformed HTTP response received from the payment init
        StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus( Mockito.any(OneyTransactionStatusRequest.class), anyBoolean() );

        // when calling the method handleSessionExpired
        PaymentResponse response = service.handleSessionExpired( createDefaultTransactionStatusRequest() );

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue( response instanceof PaymentResponseFailure );
        Assertions.assertEquals( FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure)response).getFailureCause() );
    }
    /*
    It is not necessary to perform the corresponding KO test (would be handleSessionExpired_malformedStatusResponseKO) because,
    in that case, the status response content is not parsed. Only the HTTP status code is relevant.
     */
}
