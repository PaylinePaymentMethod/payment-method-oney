package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.utils.OneyConfigBean;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationServiceTest extends OneyConfigBean {


    @Spy
    OneyHttpClient httpClient;

    @InjectMocks
    NotificationServiceImpl service;

    @BeforeAll
    public void setup() {
        service = new NotificationServiceImpl();
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void notifyTransactionStatusRequestEncrypted() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        StringResponse responseMockedPending = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul3aakmok0anPtpBvW1vZ3e83c7evaIMgKsuqlJpPjg407AoMkFm94736cZcnpC81qiX4V8n9IxMD1E50QBAOkMZ1S8Pf90kxhXSDe3wt4J13\"}");

        Mockito.doReturn(responseMockedPending).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());


        OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference(OneyConstants.EXTERNAL_REFERENCE_TYPE + OneyConstants.PIPE + "455454545415451198119")
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withCallParameters(map)
                .build();

        StringResponse transactStatus = this.httpClient.initiateGetTransactionStatus(request);

        mockCorrectlyConfigPropertiesEnum(true);
        TransactionStatusResponse resp = createTransactionStatusResponseFromJson(transactStatus.getContent(), request.getEncryptKey());

        Assertions.assertNotNull(transactStatus.getCode());
        Assertions.assertEquals("Waiting for customer validation", resp.getStatusPurchase().getStatusLabel());
        Assertions.assertEquals("PENDING", resp.getStatusPurchase().getStatusCode());

    }

    @Test
    public void notifyTransactionStatusRequestNotEncrypted() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        StringResponse responseMockedPending = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"PENDING\",\"status_label\":\"Waiting for customer validation\"}}");

        Mockito.doReturn(responseMockedPending).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());


        OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference(OneyConstants.EXTERNAL_REFERENCE_TYPE + OneyConstants.PIPE + "455454545415451198119")
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withCallParameters(map)
                .build();

        StringResponse transactStatus = this.httpClient.initiateGetTransactionStatus(request);
        mockCorrectlyConfigPropertiesEnum(false);
        TransactionStatusResponse resp = createTransactionStatusResponseFromJson(transactStatus.getContent(), request.getEncryptKey());

        Assertions.assertNotNull(transactStatus.getCode());
        Assertions.assertEquals("Waiting for customer validation", resp.getStatusPurchase().getStatusLabel());
        Assertions.assertEquals("PENDING", resp.getStatusPurchase().getStatusCode());

    }
}
