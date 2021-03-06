package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.reset.request.ResetRequest;
import com.payline.pmapi.bean.reset.response.ResetResponse;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseFailure;
import com.payline.pmapi.bean.reset.response.impl.ResetResponseSuccess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.bean.common.PurchaseStatus.StatusCode.CANCELLED;
import static com.payline.payment.oney.utils.TestUtils.createDefaultResetRequest;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

public class ResetServiceImplTest {
    private String responseOK = "{\"purchase\":{\"status_code\":\"CANCELLED\",\"status_label\":\"Transaction is completed\"}}";
    private String responseOkCiphered = "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ulzsMg0QfZ1N009CwI1PLZzBnbfv6/Enufe5TriN1gKQkEmbMYU0PMtHdk+eF7boW/lsIc5PmjpFX1E/4MUJGkzI=\"}";
    private String responseKOCiphered = "{\"encrypted_message\":\"ymDHJ7HBRe49whKjH1HDtA==\"}";

    @InjectMocks
    @Spy
    ResetServiceImpl service;

    @Spy
    OneyHttpClient httpClient;

    @BeforeEach
    public void setup() {
        final Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(OneyHttpClient.KEY_CONNECT_TIMEOUT,"2000");
        partnerConfigurationMap.put(OneyHttpClient.CONNECTION_REQUEST_TIMEOUT,"3000");
        partnerConfigurationMap.put(OneyHttpClient.READ_SOCKET_TIMEOUT,"4000");

        httpClient = OneyHttpClient.getInstance(new PartnerConfiguration(partnerConfigurationMap, new HashMap<>()));

        service = new ResetServiceImpl();
        MockitoAnnotations.initMocks(this);
        doReturn(httpClient).when(service).getNewHttpClientInstance(any());
    }


    @Test
    void resetRequestTestOK() throws Exception {
        StringResponse responseMocked1 = createStringResponse(200, "OK", responseOK);
        Mockito.doReturn(responseMocked1).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());

        StringResponse responseMocked = createStringResponse(200, "OK", responseOK);
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        ResetResponse response = service.resetRequest(createDefaultResetRequest());

        Assertions.assertEquals(ResetResponseSuccess.class, response.getClass());
        ResetResponseSuccess success = (ResetResponseSuccess) response;
        Assertions.assertEquals(CANCELLED.name(), success.getStatusCode());
        Assertions.assertNotNull(success.getPartnerTransactionId());
    }

    @Test
    void resetRequestTestKO() throws Exception {
        StringResponse responseMocked1 = createStringResponse(200, "OK", responseOkCiphered);
        Mockito.doReturn(responseMocked1).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());

        StringResponse responseMocked = createStringResponse(200, "OK", responseKOCiphered);
        Mockito.doReturn(responseMocked).when(httpClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());

        ResetRequest resetReq = createDefaultResetRequest();
        ResetResponse response = service.resetRequest(resetReq);

        Assertions.assertSame(response.getClass(), ResetResponseFailure.class);
        ResetResponseFailure fail = (ResetResponseFailure) response;
        Assertions.assertEquals(FailureCause.REFUSED, fail.getFailureCause());
        Assertions.assertEquals(resetReq.getOrder().getReference(), fail.getPartnerTransactionId());
    }
}
