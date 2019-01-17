package com.payline.payment.oney.service;

import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.bean.response.TransactionStatusResponse;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.service.impl.NotificationServiceImpl;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.bean.response.TransactionStatusResponse.createTransactionStatusResponseFromJson;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotificationServiceTest {


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
    public void notifyTransactionStatusRequestTest_nullEncryptKey() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            StringResponse responseMockedPending = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul3aakmok0anPtpBvW1vZ3e83c7evaIMgKsuqlJpPjg407AoMkFm94736cZcnpC81qiX4V8n9IxMD1E50QBAOkMZ1S8Pf90kxhXSDe3wt4J13\"}");
            StringResponse responseMockedFavorable = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");

            Mockito.doReturn(responseMockedPending).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());


            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                    .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                    .withPurchaseReference("CMDE" + OneyConstants.PIPE + "455454545415451198119")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .build();
        });

    }

    @Test
    public void notifyTransactionStatusRequestTest_emptyEncryptKey() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            StringResponse responseMockedPending = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul3aakmok0anPtpBvW1vZ3e83c7evaIMgKsuqlJpPjg407AoMkFm94736cZcnpC81qiX4V8n9IxMD1E50QBAOkMZ1S8Pf90kxhXSDe3wt4J13\"}");
            StringResponse responseMockedFavorable = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");

            Mockito.doReturn(responseMockedPending).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());


            OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode("FR")
                    .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                    .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                    .withPurchaseReference("CMDE" + OneyConstants.PIPE + "455454545415451198119")
                    .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                    .withCallParameters(new HashMap<>())
                    .build();
        });

    }

    @Test
    public void notifyTransactionStatusRequestTest() throws DecryptException, IOException, URISyntaxException {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");
        StringResponse responseMockedPending = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul3aakmok0anPtpBvW1vZ3e83c7evaIMgKsuqlJpPjg407AoMkFm94736cZcnpC81qiX4V8n9IxMD1E50QBAOkMZ1S8Pf90kxhXSDe3wt4J13\"}");
        StringResponse responseMockedFavorable = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul/bQBJ3C1/cyjmvmAAmMq9gLttO54jS+b/UB/MPwY6YeiFWc7TtYNuIHJF3Grkl2/O4B6r4zkTpus9DrEZIou4aE8tfX+G43n2zFDAoYG3u3\"}");

        Mockito.doReturn(responseMockedPending).when(httpClient).doGet(Mockito.anyString(), Mockito.anyMap());


        OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference("CMDE" + OneyConstants.PIPE + "455454545415451198119")
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withCallParameters(map)
                .build();

        StringResponse transactStatus = this.httpClient.initiateGetTransactionStatus(request);
        TransactionStatusResponse resp = createTransactionStatusResponseFromJson(transactStatus.getContent(), request.getEncryptKey());

        Assertions.assertNotNull(transactStatus.getCode());
        Assertions.assertEquals("Waiting for customer validation", resp.getStatusPurchase().getStatusLabel());
        Assertions.assertEquals("PENDING", resp.getStatusPurchase().getStatusCode());

    }
}
