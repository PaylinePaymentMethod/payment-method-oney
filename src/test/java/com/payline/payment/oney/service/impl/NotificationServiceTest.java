package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureTransactionStatus;
import com.payline.pmapi.bean.common.SuccessTransactionStatus;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.notification.response.NotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.IgnoreNotificationResponse;
import com.payline.pmapi.bean.notification.response.impl.TransactionStateChangedResponse;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    public void setupNotificationRequestBuilder(){
        requestBuilder = NotificationRequest.NotificationRequestBuilder.aNotificationRequest()
                .withHeaderInfos(new HashMap<>())
                .withPathInfo("thisIsAPath")
                .withHttpMethod("POST")
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withPartnerConfiguration(TestUtils.createDefaultPartnerConfiguration())
                .withEnvironment(TestUtils.TEST_ENVIRONMENT);
    }

    private static Stream<Arguments> parseSet() {
        return Stream.of(
                Arguments.of("FUNDED", SuccessTransactionStatus.class),
                Arguments.of("FAVORABLE", SuccessTransactionStatus.class),
                Arguments.of("REFUSED", FailureTransactionStatus.class),
                Arguments.of("ABORTED", FailureTransactionStatus.class)
        );
    }

    @ParameterizedTest
    @MethodSource("parseSet")
    void parse(String OneyStatus, Class expectedClass) throws Exception {
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
                "  \"psp_context\": \"1234\"" +
                "}";
        content = content.replace("XXXXXX", OneyStatus);

        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream(content.getBytes()))
                .build();

        StringResponse responseMockedConfirm = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"a label\"}}");
        Mockito.doReturn(responseMockedConfirm).when(client).initiateConfirmationPayment(any(), anyBoolean());

        NotificationResponse response = service.parse(request);
        TransactionStateChangedResponse transactionStateChangedResponse = (TransactionStateChangedResponse) response;

        assertEquals(expectedClass ,transactionStateChangedResponse.getTransactionStatus().getClass());
    }

    @Test
    void parseOther(){
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
                "    \"status_code\": \"ANYTHING\"," +
                "    \"status_label\": \"a status label\"," +
                "    \"reason_code\": \"aReason\"," +
                "    \"reason_label\": \"aLabel\"" +
                "  }," +
                "  \"customer\": {" +
                "    \"customer_external_code\": \"aCode\"" +
                "  }," +
                "  \"merchant_context\": \"aContext\"," +
                "  \"psp_context\": \"1234\"" +
                "}";
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream(content.getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        assertEquals(IgnoreNotificationResponse.class, response.getClass());
    }

    @Test
    void parseException(){
        NotificationRequest request = requestBuilder
                .withContent(new ByteArrayInputStream("foo".getBytes()))
                .build();

        NotificationResponse response = service.parse(request);
        TransactionStateChangedResponse stateChangedResponse = (TransactionStateChangedResponse) response;
        assertEquals(FailureTransactionStatus.class, stateChangedResponse.getTransactionStatus().getClass());
        assertNotNull( ((FailureTransactionStatus)stateChangedResponse.getTransactionStatus()).getFailureCause() );
    }

    /**
     * Test the case in which the notification request contains a properly formatted JSON with an unexpected structure
     * that cannot be cast into {@link com.payline.payment.oney.bean.response.OneyNotificationResponse}.
     * In that case, Gson returns an empty object, containing only null attributes.
     * @see https://github.com/google/gson/issues/188
     * @see https://payline.atlassian.net/browse/PAYLAPMEXT-165
     */
    @Test
    void parse_unexpectedFields(){
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
        TransactionStateChangedResponse stateChangedResponse = (TransactionStateChangedResponse) response;
        assertEquals(FailureTransactionStatus.class, stateChangedResponse.getTransactionStatus().getClass());
        assertNotNull( ((FailureTransactionStatus)stateChangedResponse.getTransactionStatus()).getFailureCause() );
    }

}
