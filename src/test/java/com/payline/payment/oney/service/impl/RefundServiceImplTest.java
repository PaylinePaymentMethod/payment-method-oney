package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.PurchaseStatus;
import com.payline.payment.oney.bean.request.OneyRefundRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.refund.response.RefundResponse;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseFailure;
import com.payline.pmapi.bean.refund.response.impl.RefundResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.FUNDED;
import static com.payline.payment.oney.utils.TestUtils.createDefaultRefundRequest;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RefundServiceImplTest extends OneyConfigBean {
    private String responseOk = "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"Transaction is completed\"}}";
    private String responseOkCiphered = "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}";
    private String responseKOCiphered = "{\"encrypted_message\":\"ymDHJ7HBRe49whKjH1HDtA==\"}";

    @InjectMocks
    @Spy
    public RefundServiceImpl service;

    OneyHttpClient httpClient;

    @BeforeEach
    public void setup() {
        final Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(OneyHttpClient.KEY_CONNECT_TIMEOUT,"2000");
        partnerConfigurationMap.put(OneyHttpClient.CONNECTION_REQUEST_TIMEOUT,"3000");
        partnerConfigurationMap.put(OneyHttpClient.READ_SOCKET_TIMEOUT,"4000");
        httpClient = Mockito.spy(OneyHttpClient.getInstance(new PartnerConfiguration(partnerConfigurationMap, new HashMap<>())));
        MockitoAnnotations.initMocks(this);
        doReturn(httpClient).when(service).getNewHttpClientInstance(any());
    }

    @Test
    public void refundRequestTestOK() throws Exception {
        StringResponse responseMocked1 = createStringResponse(200, "OK", responseOk);
        Mockito.doReturn(responseMocked1).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());

        StringResponse responseMocked = createStringResponse(200, "OK", responseOk);
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        RefundResponse response = service.refundRequest(createDefaultRefundRequest());

        Assertions.assertEquals(RefundResponseSuccess.class, response.getClass());
        RefundResponseSuccess success = (RefundResponseSuccess) response;
        Assertions.assertEquals("FUNDED", success.getStatusCode());
        Assertions.assertNotNull(success.getPartnerTransactionId());

    }

    @Test
    public void refundRequestTestKO() throws Exception {
        StringResponse responseMocked1 = createStringResponse(200, "OK", responseOkCiphered);
        Mockito.doReturn(responseMocked1).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());

        StringResponse responseMocked = createStringResponse(200, "OK", responseKOCiphered);
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        RefundRequest refundReq = createDefaultRefundRequest();
        RefundResponse response = service.refundRequest(refundReq);

        Assertions.assertSame(response.getClass(), RefundResponseFailure.class);
        RefundResponseFailure fail = (RefundResponseFailure) response;
        Assertions.assertEquals(FailureCause.REFUSED, fail.getFailureCause());
        Assertions.assertEquals(refundReq.getOrder().getReference(), fail.getPartnerTransactionId());
    }

    @Test
    public void handleStatusRequestEncrypted() throws Exception {
        StringResponse responseMocked1 = createStringResponse(200, "OK", responseOkCiphered);
        Mockito.doReturn(responseMocked1).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());
        RefundRequest refundReq = createDefaultRefundRequest();
        mockCorrectlyConfigPropertiesEnum(true);
        PurchaseStatus.StatusCode status = service.handleStatusRequest(refundReq);

        Assertions.assertEquals(FUNDED, status);
    }

    @Test
    public void handleStatusRequestNotEncrypted() throws Exception {
        StringResponse responseMocked1 = createStringResponse(200, "OK", responseOk);
        Mockito.doReturn(responseMocked1).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());
        RefundRequest refundReq = createDefaultRefundRequest();
        mockCorrectlyConfigPropertiesEnum(false);
        PurchaseStatus.StatusCode status = service.handleStatusRequest(refundReq);

        Assertions.assertEquals(FUNDED, status);
    }


    @Test
    public void refundRequest_malformedStatusResponseOK() throws PluginTechnicalException {
        // given a malformed HTTP response received to the status request
        StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());

        // when calling the method refundRequest
        RefundResponse response = service.refundRequest(createDefaultRefundRequest());

        // then a RefundResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue(response instanceof RefundResponseFailure);
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, ((RefundResponseFailure) response).getFailureCause());
    }
    /*
    It is not necessary to perform the corresponding KO test (would be refundRequest_malformedStatusResponseKO) because,
    in that case, the status response content is not parsed. Only the HTTP status code is relevant.
     */

    @Test
    public void refundRequest_malformedRefundResponseKO() throws PluginTechnicalException {


        StringResponse response1Mocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(response1Mocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());

        // given a malformed HTTP response received to the refund/cancel request
        StringResponse responseMocked = createStringResponse(404, "Bad Request", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiateRefundPayment(Mockito.any(), anyBoolean());


        // when calling the method refundRequest
        RefundResponse response = service.refundRequest(createDefaultRefundRequest());

        // then a RefundResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue(response instanceof RefundResponseFailure);
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, ((RefundResponseFailure) response).getFailureCause());
    }

    @Test
    public void refundRequest_malformedRefundResponseOK() throws PluginTechnicalException {

        StringResponse response1Mocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(response1Mocked).when(httpClient).initiateGetTransactionStatus(Mockito.any(), anyBoolean());

        // given a malformed HTTP response received to the refund/cancel request
        StringResponse responseMocked = createStringResponse(200, "OK", "[]");
        Mockito.doReturn(responseMocked).when(httpClient).initiateRefundPayment(Mockito.any(), anyBoolean());

        // when calling the method refundRequest
        RefundResponse response = service.refundRequest(createDefaultRefundRequest());

        // then a RefundResponseFailure with the FailureCause.COMMUNICATION_ERROR is returned
        Assertions.assertTrue(response instanceof RefundResponseFailure);
        Assertions.assertEquals(FailureCause.COMMUNICATION_ERROR, ((RefundResponseFailure) response).getFailureCause());
    }
}
