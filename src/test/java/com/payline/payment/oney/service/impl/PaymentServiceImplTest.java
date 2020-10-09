package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyPaymentRequest;
import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.PaymentResponse;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static com.payline.payment.oney.utils.TestUtils.createCompletePaymentRequestBuilder;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;
import static org.mockito.ArgumentMatchers.anyBoolean;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class PaymentServiceImplTest extends OneyConfigBean {

    @Spy
    OneyHttpClient httpClient;

    @InjectMocks
    PaymentServiceImpl service;

    @BeforeEach
    public void setup() {
        service = new PaymentServiceImpl();
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void paymentRequestOKEncrypted() throws HttpCallException {

        StringResponse responseEncryptedMocked = createStringResponse(200, "", "{\"encrypted_message\": \"FhzjXBU2Ek+/dmCMVB4wWn6ytL2+dh5mIx+gxDtcp4rTSzO/LA1Q72aClEvNoeXVdc3wg8L8PpMvAhRkWkLc1DyuX14icAZP8C7uA5COgRIzklUPJq/d9tiDWXxszS9o4ALbCfpGYqSgUN38fBnJhC9Y7RBqY4eq+H0iTRtvfYSLmKumsYvQFJY/21j+Xou/ZLppruwA6/MNC0nDGXw2o2PJeMGm+e5i4lUlqowvecmZ+GWQM91pOrb95B/pqriDYwZnnRQrewuhAyvIkR8LVQ==\"}");
        Mockito.doReturn(responseEncryptedMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        mockCorrectlyConfigPropertiesEnum(true);
        PaymentResponseRedirect response = (PaymentResponseRedirect) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response.getRedirectionRequest().getUrl());
        Assertions.assertNotNull(response.getRequestContext().getRequestData().get(OneyConstants.MERCHANT_GUID_KEY));

    }

    @Test
    void paymentRequestOKNotEncrypted() throws HttpCallException {

        StringResponse responseEncryptedMocked = createStringResponse(200, "", "{\"returned_url\": \"https://pplogin.oney.be/Subscription/PaymentPage_Entry.aspx?Token=PlzTT7EsMCuFilPzV6XS2HUmLiJ7R25hibsGy4BBJ7YXWprwJoNO4hRmttwx5x8%2fOttm5IcgMOUlZ6OUCV8mxIQyjjGSM0a88BqhGfoo6oc%3d\"}");
        Mockito.doReturn(responseEncryptedMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        mockCorrectlyConfigPropertiesEnum(false);
        PaymentResponseRedirect response = (PaymentResponseRedirect) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response.getRedirectionRequest().getUrl());
        Assertions.assertNotNull(response.getRequestContext().getRequestData().get(OneyConstants.MERCHANT_GUID_KEY));

    }

    @Test
    void paymentRequestKOEncrypted() throws HttpCallException {
        StringResponse responseMocked = createStringResponse(400, "Bad request", "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"purchase.delivery.delivery_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"customer.customer_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"payment.payment_type\",\"error_code\":\"ERR_03\",\"error_label\":\"Format of the field is invalid [{Integer}]\"}]}}");

        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        mockCorrectlyConfigPropertiesEnum(true);
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400 - ERR_02 - purchase.delivery.delivery_address.", response.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_FIELD_FORMAT, response.getFailureCause());

    }

    @Test
    void paymentRequestKONotEncrypted() throws HttpCallException {
        StringResponse responseMocked = createStringResponse(400, "Bad request", "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"purchase.delivery.delivery_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"customer.customer_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"payment.payment_type\",\"error_code\":\"ERR_03\",\"error_label\":\"Format of the field is invalid [{Integer}]\"}]}}");

        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        mockCorrectlyConfigPropertiesEnum(false);
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400 - ERR_02 - purchase.delivery.delivery_address.", response.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_FIELD_FORMAT, response.getFailureCause());

    }

    @Test
    void paymentRequestKO_PAYLAPMEXT_42() throws Exception {

        StringResponse responseMocked = createStringResponse(400, "Bad request", "{\n" +
                "  \"Payments_Error_Response\": {\n" +
                "    \"error_list\": [\n" +
                "      {\n" +
                "        \"error\": {\n" +
                "          \"field\": \"customer.identity.person_type\",\n" +
                "          \"error_code\": \"ERR_04\",\n" +
                "          \"error_label\": \"Value of the field is invalid [{Integer}]\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");

        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400 - ERR_04 - customer.identity.person_type", response.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, response.getFailureCause());

        // test du format décrit sous confluence (sans objet 'error'
        StringResponse responseMocked2 = createStringResponse(400, "Bad request", "{\n" +
                "  \"Payments_Error_Response\": {\n" +
                "    \"error_list\": [\n" +
                "      {\n" +
                "          \"field\": \"customer.identity.person_type\",\n" +
                "          \"error_code\": \"ERR_04\",\n" +
                "          \"error_label\": \"Value of the field is invalid [{Integer}]\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}");

        Mockito.doReturn(responseMocked2).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400 - ERR_04 - customer.identity.person_type", response.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, response.getFailureCause());

    }

    @Test
    void paymentRequestKO404() throws HttpCallException {
        StringResponse responseMocked = createStringResponse(404, "Bad request", "{Payments_Error_Response:{error_list:[{field:purchase.delivery.delivery_address.country_code,error_code:ERR_02,error_label:\"Size of the field should be equal to [3] characters\"},{field:purchase.item_list.category_code,error_code:ERR_04,error_label:\"Value of the field is invalid [{Integer}]\"},{field:purchase.item_list.category_code,error_code:ERR_04,error_label:\"Value of the field is invalid [{Integer}]\"},{field:customer.customer_address.country_code,error_code:ERR_02,error_label:\"Size of the field should be equal to [3] characters\"},{field:payment.payment_type,error_code:ERR_03,error_label:\"Format of the field is invalid [{Integer}]\"}]}}");

        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentRequestBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("404 - ERR_02 - purchase.delivery.delivery_address.", response.getErrorCode());
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, response.getFailureCause());

    }

    @Test
    void paymentRequest_malformedInitiatePaymentResponseKO() throws PluginTechnicalException {
        // given a malformed HTTP response received from the payment init
        StringResponse responseMocked = createStringResponse(404, "Bad request", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiatePayment( Mockito.any(OneyPaymentRequest.class), anyBoolean() );

        // when calling method paymentRequest
        PaymentResponse response = service.paymentRequest( createCompletePaymentRequestBuilder().build() );

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue( response instanceof PaymentResponseFailure );
        Assertions.assertEquals( FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure)response).getFailureCause() );
    }

    @Test
    void paymentRequest_malformedInitiatePaymentResponseOK() throws PluginTechnicalException {
        // given a malformed HTTP response received from the payment init
        StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiatePayment( Mockito.any(OneyPaymentRequest.class), anyBoolean() );

        // when calling method paymentRequest
        PaymentResponse response = service.paymentRequest( createCompletePaymentRequestBuilder().build() );

        // then a PaymentResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue( response instanceof PaymentResponseFailure );
        Assertions.assertEquals( FailureCause.COMMUNICATION_ERROR, ((PaymentResponseFailure)response).getFailureCause() );
    }

}
