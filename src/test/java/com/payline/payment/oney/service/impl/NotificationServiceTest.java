package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.common.FailureTransactionStatus;
import com.payline.pmapi.bean.common.OnHoldTransactionStatus;
import com.payline.pmapi.bean.common.SuccessTransactionStatus;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.PaymentResponseByNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseOnHold;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseSuccess;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.stream.Stream;

import static com.payline.payment.oney.utils.TestUtils.createStringResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationServiceTest extends OneyConfigBean {

    @InjectMocks
    NotificationServiceImpl service;

    @Spy
    OneyHttpClient client;

    private NotificationRequest.NotificationRequestBuilder requestBuilder;

    @BeforeAll
    public void setup() {
        service = new NotificationServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @BeforeEach
    public void setupNotificationRequestBuilder() {
        requestBuilder = NotificationRequest.NotificationRequestBuilder.aNotificationRequest()
                .withHeaderInfos(new HashMap<>())
                .withPathInfo("thisIsAPath")
                .withHttpMethod("POST")
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withPartnerConfiguration(TestUtils.createDefaultPartnerConfiguration())
                .withEnvironment(TestUtils.TEST_ENVIRONMENT);
    }

    private static Stream<Arguments> parse_nonExistingTransaction_set() {
        return Stream.of(
                Arguments.of("FUNDED", PaymentResponseSuccess.class),
                Arguments.of("CANCELLED", PaymentResponseSuccess.class),
                Arguments.of("FAVORABLE", PaymentResponseSuccess.class),
                Arguments.of("REFUSED", PaymentResponseFailure.class),
                Arguments.of("ABORTED", PaymentResponseFailure.class),
                Arguments.of("PENDING", PaymentResponseOnHold.class),
                Arguments.of("TO_BE_FUNDED", PaymentResponseSuccess.class)
        );
    }

    @ParameterizedTest
    @MethodSource("parse_nonExistingTransaction_set")
    void parse_nonExistingTransaction(String oneyStatus, Class expectedClass) throws Exception {
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream(mockContent(oneyStatus).getBytes()))
                .build();

        StringResponse responseMockedConfirm = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"a label\"}}");
        Mockito.doReturn(responseMockedConfirm).when(client).initiateConfirmationPayment(any(), anyBoolean());

        NotificationResponse response = service.parse(request);
        assertTrue( response instanceof PaymentResponseByNotificationResponse );
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;

        assertEquals(expectedClass, paymentResponseByNotificationResponse.getPaymentResponse().getClass());
    }

    private static Stream<Arguments> parse_existingTransaction_set() {
        return Stream.of(
                Arguments.of("FUNDED", SuccessTransactionStatus.class),
                Arguments.of("CANCELLED", SuccessTransactionStatus.class),
                Arguments.of("FAVORABLE", SuccessTransactionStatus.class),
                Arguments.of("REFUSED", FailureTransactionStatus.class),
                Arguments.of("ABORTED", FailureTransactionStatus.class),
                Arguments.of("PENDING", OnHoldTransactionStatus.class),
                Arguments.of("TO_BE_FUNDED", SuccessTransactionStatus.class)
        );
    }

    @ParameterizedTest
    @MethodSource("parse_existingTransaction_set")
    void parse_existingTransaction(String oneyStatus, Class expectedClass) throws Exception {
        NotificationRequest request = requestBuilder
                .withTransactionId("G1906171638279792")
                .withContent(new ByteArrayInputStream(mockContent(oneyStatus).getBytes()))
                .build();

        StringResponse responseMockedConfirm = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"a label\"}}");
        Mockito.doReturn(responseMockedConfirm).when(client).initiateConfirmationPayment(any(), anyBoolean());

        NotificationResponse response = service.parse(request);
        assertTrue( response instanceof TransactionStateChangedResponse );
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;

        assertEquals(expectedClass, transactionStateChangedResponse.getTransactionStatus().getClass());
    }

    @Test
    void parse_nonExistingTransaction_confirmationFail() throws Exception {
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream(mockContent("FAVORABLE").getBytes()))
                .build();

        StringResponse responseMockedConfirm = createStringResponse(200, "OK", "this is not a good content");
        Mockito.doReturn(responseMockedConfirm).when(client).initiateConfirmationPayment(any(), anyBoolean());

        NotificationResponse response = service.parse(request);
        assertTrue( response instanceof PaymentResponseByNotificationResponse );
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        assertEquals(FailureCause.COMMUNICATION_ERROR,responseFailure.getFailureCause());
    }

    @Test
    void parse_existingTransaction_confirmationFail() throws Exception {
        NotificationRequest request = requestBuilder
                .withTransactionId("G1906171638279792")
                .withContent(new ByteArrayInputStream(mockContent("FAVORABLE").getBytes()))
                .build();

        StringResponse responseMockedConfirm = createStringResponse(200, "OK", "this is not a good content");
        Mockito.doReturn(responseMockedConfirm).when(client).initiateConfirmationPayment(any(), anyBoolean());

        NotificationResponse response = service.parse(request);
        assertTrue( response instanceof TransactionStateChangedResponse );
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        assertEquals(FailureTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
    }

    @Test
    void parseOther() {
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream(mockContent("ANYTHING").getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        assertEquals(IgnoreNotificationResponse.class, response.getClass());
    }

    @Test
    void parse_nonExistingTransaction_exception() {
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream("foo".getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        assertNotNull(responseFailure.getFailureCause());
    }

    @Test
    void parse_existingTransaction_exception() {
        NotificationRequest request = requestBuilder
                .withTransactionId("G1906171638279792")
                .withContent(new ByteArrayInputStream("foo".getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        assertEquals(FailureTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
    }

    @Test
    void parse_existingTransaction_wrongTransactionId(){
        NotificationRequest request = requestBuilder
                .withTransactionId("123456")
                .withContent(new ByteArrayInputStream(mockContent("FAVORABLE").getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;
        assertEquals(FailureTransactionStatus.class, transactionStateChangedResponse.getTransactionStatus().getClass());
    }

    /**
     * Test the case in which the notification request contains a properly formatted JSON with an unexpected structure
     * that cannot be cast into {@link com.payline.payment.oney.bean.response.OneyNotificationResponse}.
     * In that case, Gson returns an empty object, containing only null attributes.
     *
     * @see https://github.com/google/gson/issues/188
     * @see https://payline.atlassian.net/browse/PAYLAPMEXT-165
     */
    @Test
    void parse_unexpectedFields() {
        String content = "{" +
                "  \"merchant_id\": \"18093\"," +
                "  \"psp_id\": \"200000782\"," +
                "  \"server_response_url\": \"https://webpayment.dev.payline.com/payline-widget/pmapiNotification/ONEY/v/…\"," +
                "  \"message\": {" +
                "    \"language_code\": \"fr\"," +
                "    \"merchant_guid\": \"6fd0d7f8123b4a729cb74a89f32e6035\"," +
                "    \"purchase\": {" +
                "      \"external_reference_type\": \"CMDE\"," +
                "      \"external_reference\": \"NR_ONEY_FR_TC1_W3025_PENDING_170619163818\"," +
                "      \"status_code\": \"PENDING\"," +
                "      \"status_label\": \"La demande de paiement est en cours d’étude.\"" +
                "    }," +
                "    \"customer\": {" +
                "      \"customer_external_code\": \"client\"" +
                "    }," +
                "    \"merchant_context\": \"\"," +
                "    \"psp_context\": \"G1906171638279792\"" +
                "  }," +
                "  \"crypting_method\": \"\"" +
                "}";
        content = content.replace("XXXXXX", "ANYTHING");
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream(content.getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        PaymentResponseByNotificationResponse paymentResponseByNotificationResponse = (PaymentResponseByNotificationResponse) response;
        assertEquals(PaymentResponseFailure.class, paymentResponseByNotificationResponse.getPaymentResponse().getClass());
        PaymentResponseFailure responseFailure = (PaymentResponseFailure) paymentResponseByNotificationResponse.getPaymentResponse();
        assertNotNull(responseFailure.getFailureCause());
    }

    private String mockContent( String statusCode ){
        String content = "{" +
                "  \"language_code\": \"FR\"," +
                "  \"merchant_guid\": \"anId\"," +
                "  \"oney_request_id\": \"123456789\"," +
                "  \"purchase\": {" +
                "    \"external_reference_type\": \"aType\"," +
                "    \"external_reference\": \"987654321\"," +
                "    \"purchase_merchant\": {" +
                "      \"merchant_guid\": \"azerty\"" +
                "    }," +
                "    \"status_code\": \"XXXXXX\"," +
                "    \"status_label\": \"a status label\"," +
                "    \"reason_code\": \"aReason\"," +
                "    \"reason_label\": \"aLabel\"" +
                "  }," +
                "  \"customer\": {" +
                "    \"customer_external_code\": \"aCode\"" +
                "  }," +
                "  \"merchant_context\": \"CN!1000!EUR\"," +
                "  \"psp_context\": \"G1906171638279792\"" +
                "}";
        content = content.replace("XXXXXX", statusCode);
        return content;
    }

}
