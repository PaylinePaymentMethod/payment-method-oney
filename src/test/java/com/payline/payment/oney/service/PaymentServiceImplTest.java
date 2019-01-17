package com.payline.payment.oney.service;

import com.payline.payment.oney.service.impl.PaymentServiceImpl;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;
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

import static com.payline.payment.oney.utils.TestUtils.createCompletePaymentBuilder;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PaymentServiceImplTest {


    @Spy
    OneyHttpClient httpClient;

    @InjectMocks
    PaymentServiceImpl service;


    @BeforeAll
    public void setup() {
        service = new PaymentServiceImpl();
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void paymentRequestOK() throws IOException, URISyntaxException {

        StringResponse responseEncryptedMocked = createStringResponse(200, "", "{\"encrypted_message\": \"FhzjXBU2Ek+/dmCMVB4wWn6ytL2+dh5mIx+gxDtcp4rTSzO/LA1Q72aClEvNoeXVdc3wg8L8PpMvAhRkWkLc1DyuX14icAZP8C7uA5COgRIzklUPJq/d9tiDWXxszS9o4ALbCfpGYqSgUN38fBnJhC9Y7RBqY4eq+H0iTRtvfYSLmKumsYvQFJY/21j+Xou/ZLppruwA6/MNC0nDGXw2o2PJeMGm+e5i4lUlqowvecmZ+GWQM91pOrb95B/pqriDYwZnnRQrewuhAyvIkR8LVQ==\"}");
        Mockito.doReturn(responseEncryptedMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        PaymentResponseRedirect response = (PaymentResponseRedirect) service.paymentRequest(createCompletePaymentBuilder().build());
        Assertions.assertNotNull(response.getRedirectionRequest().getUrl());
        Assertions.assertNotNull(response.getRequestContext().getRequestData().get(OneyConstants.MERCHANT_GUID_KEY));

    }

    @Test
    public void paymentRequestKO() throws IOException, URISyntaxException {
        StringResponse responseMocked = createStringResponse(400, "Bad request", "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"purchase.delivery.delivery_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"customer.customer_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"payment.payment_type\",\"error_code\":\"ERR_03\",\"error_label\":\"Format of the field is invalid [{Integer}]\"}]}}");

        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400", response.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_FIELD_FORMAT, response.getFailureCause());

    }

    @Test
    public void paymentRequestKO_PAYLAPMEXT_42() throws Exception {

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
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400", response.getErrorCode());
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
        response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("400", response.getErrorCode());
        Assertions.assertEquals(FailureCause.INVALID_DATA, response.getFailureCause());

    }

    @Test
    public void paymentRequestKO404() throws IOException, URISyntaxException {
        StringResponse responseMocked = createStringResponse(404, "Bad request", "{Payments_Error_Response:{error_list:[{field:purchase.delivery.delivery_address.country_code,error_code:ERR_02,error_label:\"Size of the field should be equal to [3] characters\"},{field:purchase.item_list.category_code,error_code:ERR_04,error_label:\"Value of the field is invalid [{Integer}]\"},{field:purchase.item_list.category_code,error_code:ERR_04,error_label:\"Value of the field is invalid [{Integer}]\"},{field:customer.customer_address.country_code,error_code:ERR_02,error_label:\"Size of the field should be equal to [3] characters\"},{field:payment.payment_type,error_code:ERR_03,error_label:\"Format of the field is invalid [{Integer}]\"}]}}");

        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
        PaymentResponseFailure response = (PaymentResponseFailure) service.paymentRequest(createCompletePaymentBuilder().build());
        Assertions.assertNotNull(response);
        Assertions.assertEquals("404", response.getErrorCode());
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, response.getFailureCause());

    }


}
