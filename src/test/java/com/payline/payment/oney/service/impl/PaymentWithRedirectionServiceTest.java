package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.*;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentWithRedirectionServiceTest extends OneyConfigBean {

    private final String partnerTransactionId = "123456789A";

    @InjectMocks
    @Spy
    public PaymentWithRedirectionServiceImpl underTest;

    OneyHttpClient httpClient;

    @BeforeEach
    public void setup() {
        underTest = new PaymentWithRedirectionServiceImpl();
        final Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(OneyHttpClient.KEY_CONNECT_TIMEOUT,"2000");
        partnerConfigurationMap.put(OneyHttpClient.CONNECTION_REQUEST_TIMEOUT,"3000");
        partnerConfigurationMap.put(OneyHttpClient.READ_SOCKET_TIMEOUT,"4000");

        httpClient = Mockito.spy(OneyHttpClient.getInstance(new PartnerConfiguration(partnerConfigurationMap, new HashMap<>())));

        MockitoAnnotations.initMocks(this);
        doReturn(httpClient).when(underTest).getNewHttpClientInstance(any(RedirectionPaymentRequest.class));
        doReturn(httpClient).when(underTest).getNewHttpClientInstance(any(TransactionStatusRequest.class));
    }


    @Test
    void confirmPaymentTest() throws Exception {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        doReturn(responseMocked).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        final PaymentResponse response = underTest.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference(), httpClient);

        if (response.getClass() == PaymentResponseSuccess.class) {
            final PaymentResponseSuccess success = (PaymentResponseSuccess) response;
            assertEquals("200", success.getStatusCode());
            assertEquals("Transaction is completed", success.getMessage().getMessage());
            assertNotNull(success.getTransactionAdditionalData());
        }

    }

    @Test
    void confirmPaymentTestInvalidData() throws Exception {
        final StringResponse responseMocked = createStringResponse(400, "Bad Request", "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"Merchant_request_id\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}]}}");
        doReturn(responseMocked).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        final PaymentResponse response = underTest.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference(), httpClient);
        final PaymentResponseFailure fail = (PaymentResponseFailure) response;
        assertEquals("400 - ERR_04 - Merchant_request_id", fail.getErrorCode());
        assertEquals(FailureCause.INVALID_DATA, fail.getFailureCause());
    }


    @Test
    void confirmPaymentTestNotFound() throws Exception {
        final StringResponse responseMocked = createStringResponse(404, "Not Found", "{\"statusCode\": 404, \"message\": \"Resource not found\"}");
        doReturn(responseMocked).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        final PaymentResponseFailure response = (PaymentResponseFailure) underTest.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference(), httpClient);
        assertEquals("404", response.getErrorCode());
        assertEquals(FailureCause.COMMUNICATION_ERROR, response.getFailureCause());
    }

    @Test
    void validatePayment_malformedConfirmResponseKO() throws PluginTechnicalException {
        // given a malformed HTTP response received to the confirm request
        final StringResponse responseMocked = createStringResponse(404, "Bad request", "[]");
        doReturn(responseMocked).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        // when calling the method validatePayment
        final PaymentResponse response = underTest.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference(), httpClient);

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        assertTrue(response instanceof PaymentResponseFailure);
        assertEquals(FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure) response).getFailureCause());
    }

    @Test
    void validatePayment_malformedConfirmResponseOK() throws PluginTechnicalException {
        // given a malformed HTTP response received to the confirm request
        final StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        doReturn(responseMocked).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final OneyConfirmRequest paymentRequest = new OneyConfirmRequest.Builder(redirectionPaymentRequest)
                .build();

        // when calling the method validatePayment
        final PaymentResponse response = underTest.validatePayment(paymentRequest, true, redirectionPaymentRequest.getOrder().getReference(), httpClient);

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        assertTrue(response instanceof PaymentResponseFailure);
        assertEquals(FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure) response).getFailureCause());
    }

    @Test
    void finalizeRedirectionPaymentEncryptedOK() throws Exception {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");
        final StringResponse responseMockedConfirmation = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");

        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        doReturn(responseMockedConfirmation).when(httpClient).initiateConfirmationPayment(Mockito.any(), anyBoolean());

        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        mockCorrectlyConfigPropertiesEnum(true);
        final PaymentResponse response = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        assertEquals(PaymentResponseSuccess.class, response.getClass());
        final PaymentResponseSuccess success = (PaymentResponseSuccess) response;
        assertEquals(partnerTransactionId, success.getPartnerTransactionId());
        assertEquals("SUCCESS", success.getMessage().getType().name());
        assertEquals("200",success.getStatusCode());
        assertNotNull(success.getTransactionDetails());
    }

    @Test
    void finalizeRedirectionPaymentNotEncryptedOK() throws Exception {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Transaction is completed\"}}");
        final StringResponse responseMockedConfirmation = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"Transaction is completed\"}}");

        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        doReturn(responseMockedConfirmation).when(httpClient).initiateConfirmationPayment(Mockito.any(), anyBoolean());


        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        mockCorrectlyConfigPropertiesEnum(false);
        final PaymentResponse response = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        assertEquals(PaymentResponseSuccess.class, response.getClass());
        final PaymentResponseSuccess success = (PaymentResponseSuccess) response;
        assertEquals(partnerTransactionId, success.getPartnerTransactionId());
        assertEquals("SUCCESS", success.getMessage().getType().name());
        assertEquals("200",success.getStatusCode());
        assertNotNull(success.getTransactionDetails());
    }

    @Test
    void finalizeRedirectionPaymentTestKO() throws PluginTechnicalException {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul41+5xLG5BBT+jV4I19n1BxNgTTBkgClTslC3pM/0UXrH4z6JeeUV8SSM47yFE0zv8rcHvgxH9SoaVN4e7IL5lA=\"}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());

        mockCorrectlyConfigPropertiesEnum(true);
        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final PaymentResponse response = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        assertEquals(PaymentResponseFailure.class, response.getClass());
        final PaymentResponseFailure failure = (PaymentResponseFailure) response;
        assertNotNull(failure.getPartnerTransactionId());
        assertEquals("Purchase status : REFUSED", failure.getErrorCode());
        assertEquals(FailureCause.REFUSED, failure.getFailureCause());
    }

    @Test
    void finalizeRedirectionPaymentTestNullResponse() throws PluginTechnicalException {
        // create mocks
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Oney accepts the payment\"}}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());

        doReturn(null).when(underTest).findErrorResponse(any());

        final StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Oney accepts the payment\"}}");
        doReturn(responseMocked2).when(httpClient).initiateConfirmationPayment(Mockito.any(), anyBoolean());

        // do the call
        final RedirectionPaymentRequest redirectionPaymentRequest = createCompleteRedirectionPaymentBuilder();
        final PaymentResponse response = underTest.finalizeRedirectionPayment(redirectionPaymentRequest);

        // assertions
        assertEquals(PaymentResponseSuccess.class, response.getClass());

    }


    @Test
    void handleSessionExpiredFundedEncrypted() throws PluginTechnicalException {

        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);
        final PaymentResponseSuccess success = (PaymentResponseSuccess) paymentResponse;
        assertEquals("455454545415451198120", success.getPartnerTransactionId());
        assertEquals("SUCCESS", success.getMessage().getType().name());
        assertEquals("200",success.getStatusCode());
        assertNotNull(success.getTransactionDetails());
    }

    @Test
    void handleSessionExpiredFundedNotEncrypted() throws PluginTechnicalException {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"Transaction is completed\"}}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);
        final PaymentResponseSuccess success = (PaymentResponseSuccess) paymentResponse;
        assertEquals("455454545415451198120", success.getPartnerTransactionId());
        assertEquals("SUCCESS", success.getMessage().getType().name());
        assertEquals("200",success.getStatusCode());
        assertNotNull(success.getTransactionDetails());
    }


    @Test
    void handleSessionExpiredOnHoldEncrypted() throws PluginTechnicalException {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"Zfxsl1nYU+7gI2vAD7S+JSO1EkNNk4gaIQcX++gJrX7NfjZ417t0L7ruzUCqFyxIVQWywc2FqrUK6J4kU5EPh0ksAzV6KmKWDolDoGte7uENMlMzcTriutnu5d/fJEf1\"}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseOnHold.class);
    }

    @Test
    void handleSessionExpiredOnHoldNotEncrypted() throws PluginTechnicalException {

        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\": { \"status_code\": \"PENDING\", \"status_label\": \"Waiting for customer validation\" }}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(OneyTransactionStatusRequest.class), anyBoolean());
        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseOnHold.class);

    }

    @Test
    void handleSessionExpiredTestNullResponse() throws PluginTechnicalException {
        // create mocks
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Oney accepts the payment\"}}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());

        doReturn(null).when(underTest).findErrorResponse(any());

        final StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"Oney accepts the payment\"}}");
        doReturn(responseMocked2).when(httpClient).initiateConfirmationPayment(Mockito.any(), anyBoolean());

        // do the call
        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        final PaymentResponse response = underTest.handleSessionExpired(transactionStatusReq);

        // assertions
        assertEquals(PaymentResponseSuccess.class, response.getClass());

    }


    @Test
    void handleSessionExpiredTestRefused() throws PluginTechnicalException {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul41+5xLG5BBT+jV4I19n1BxNgTTBkgClTslC3pM/0UXrH4z6JeeUV8SSM47yFE0zv8rcHvgxH9SoaVN4e7IL5lA=\"}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(OneyTransactionStatusRequest.class), anyBoolean());
        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseFailure.class);
        final PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponse;
        assertEquals(FailureCause.REFUSED, responseFailure.getFailureCause());

    }

    @Test
    void handleSessionExpiredFavorableEncrypted() throws PluginTechnicalException {
        final StringResponse responseMocked = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        final StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}");
        doReturn(responseMocked2).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);
        final PaymentResponseSuccess success = (PaymentResponseSuccess) paymentResponse;
        assertEquals("455454545415451198120", success.getPartnerTransactionId());
        assertEquals("SUCCESS", success.getMessage().getType().name());
        assertEquals("200",success.getStatusCode());
        assertNotNull(success.getTransactionDetails());

    }

    @Test
    void handleSessionExpiredFavorableNotEncrypted() throws PluginTechnicalException {
        final StringResponse responseMocked = createStringResponse(200, "OK",  "{\"language_code\":\"fr\",\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"La demande de paiement est dans un état favorable pour financement.\"}}");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());
        final StringResponse responseMocked2 = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"Oney accepts the payment\"}}");
        doReturn(responseMocked2).when(httpClient).initiateConfirmationPayment(Mockito.any(OneyConfirmRequest.class), anyBoolean());

        final TransactionStatusRequest transactionStatusReq = createDefaultTransactionStatusRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        final PaymentResponse paymentResponse = underTest.handleSessionExpired(transactionStatusReq);
        verify(httpClient, times(1)).initiateGetTransactionStatus(any(), anyBoolean());
        verify(underTest, times(1)).validatePayment(any(), anyBoolean(), anyString(),eq(httpClient));


        assertNotNull(paymentResponse);
        assertEquals(paymentResponse.getClass(), PaymentResponseSuccess.class);
        final PaymentResponseSuccess success = (PaymentResponseSuccess) paymentResponse;
        assertEquals("455454545415451198120", success.getPartnerTransactionId());
        assertEquals("SUCCESS", success.getMessage().getType().name());
        assertEquals("200",success.getStatusCode());
        assertNotNull(success.getTransactionDetails());

    }

    @Test
    void handleSessionExpired_malformedStatusResponseOK() throws PluginTechnicalException {
        // given a malformed HTTP response received from the payment init
        final StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(OneyTransactionStatusRequest.class), anyBoolean());

        // when calling the method handleSessionExpired
        final PaymentResponse response = underTest.handleSessionExpired(createDefaultTransactionStatusRequest());

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        assertTrue(response instanceof PaymentResponseFailure);
        assertEquals(FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure) response).getFailureCause());
    }
    /*
    It is not necessary to perform the corresponding KO test (would be handleSessionExpired_malformedStatusResponseKO) because,
    in that case, the status response content is not parsed. Only the HTTP status code is relevant.
     */
}
