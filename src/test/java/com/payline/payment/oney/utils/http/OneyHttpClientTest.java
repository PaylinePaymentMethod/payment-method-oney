package com.payline.payment.oney.utils.http;

import com.payline.payment.oney.bean.common.PurchaseCancel;
import com.payline.payment.oney.bean.request.OneyRefundRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.utils.OneyConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.PARTNER_API_URL;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

@PrepareForTest(AbstractHttpClient.class)
public class OneyHttpClientTest {


    //ToDO  mock http call, not mocked now to check if they work
    @Spy
    OneyHttpClient testedClient;

    @InjectMocks
    OneyHttpClient client;

    @Mock
    CloseableHttpClient closableClient;


    private Map<String, String> params;

    @BeforeEach
    public void setup() {
        testedClient = OneyHttpClient.getInstance();
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(client, "client", closableClient);

        params = new HashMap<>();
        params.put("psp_guid", "6ba2a5e2-df17-4ad7-8406-6a9fc488a60a");
        params.put("merchant_guid", "9813e3ff-c365-43f2-8dca-94b850befbf9");
        params.put("reference", "CMDE" + OneyConstants.PIPE + "455454545415451198a");
        params.put(PARTNER_API_URL, "https://oney-staging.azure-api.net");
    }

    @Test
    public void doGet() throws IOException, URISyntaxException {

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "FINE!"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        StringResponse response = client.doGet("/staging/payments/v1/purchase/", params);

        //Assert we have a response
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getCode());

    }

    @Test
    public void doPost() throws Exception {

        String requestContent = HttpDataUtils.CREATE_REQ_BODY;
        String path = "/staging/payments/v1/purchase/facilypay_url";

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 400, "FINE!"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        StringResponse response = client.doPost(path, requestContent, params);

        //Assert we have a response
        Assertions.assertNotNull(response);
        Assertions.assertEquals(400, response.getCode());

    }

    @Test
    public void buildGetOrderPath() {


        Map<String, String> param = new HashMap<>();
        param.put("psp_guid", "val1");
        param.put("merchant_guid", "val2");
        param.put("reference", "val3");

        String pathAttempted = "somePath/psp_guid/val1/merchant_guid/val2/reference/val3";
        String path = testedClient.buildGetOrderPath("somePath", param);
        Assertions.assertEquals(pathAttempted, path);
    }

    @Test
    public void buildConfirmOrderPath() {

        Map<String, String> param = new HashMap<>();
        param.put("psp_guid", "val1");
        param.put("merchant_guid", "val2");
        param.put("reference", "val3");

        String pathAttempted = "somePath/psp_guid/val1/merchant_guid/val2/reference/val3/action/confirm";
        String path = testedClient.buildConfirmOrderPath("somePath", param);
        Assertions.assertEquals(pathAttempted, path);
    }

    @Test
    public void initiateGetTransactionStatusTest() throws IOException, URISyntaxException {

        StringResponse responseMockedOK = createStringResponse(200, "ZZOK", "{\"content\":\"{\\\"encrypted_message\\\":\\\"+l2i0o7hGRh+wJO02++ul41+5xLG5BBT+jV4I19n1BxNgTTBkgClTslC3pM/0UXrEOJt3Nv3LTMrGFG1pzsOP6gxM5c+lw57K0YUbQqoGgI\\u003d\\\"}\",\"code\":200,\"message\":\"OK\"}");
        PowerMockito.suppress(PowerMockito.methods(AbstractHttpClient.class, "doGet"));

        Mockito.doReturn(responseMockedOK).when(testedClient).doGet(Mockito.anyString(), Mockito.anyMap());


        OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference("CMDE" + OneyConstants.PIPE + "455454545415451198114")
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withCallParameters(params)
                .build();

        Assertions.assertNotNull(request);
        StringResponse transactStatus = testedClient.initiateGetTransactionStatus(request);
        Assertions.assertNotNull(transactStatus.getCode());
    }


    @Test
    public void initiateRefundRequestTest() throws DecryptException, IOException, URISyntaxException {

        StringResponse responseMockedOK = createStringResponse(200, "OK", "{\"content\":\"{\\\"encrypted_message\\\":\\\"+l2i0o7hGRh+wJO02++ul+pupX40ZlQGwcgL91laJl8Vmw5MnvB6zm+cpQviUjey0a4YEoiRButKTLyhHS8SBlDyClrx8GM0AWSp0+DsthbblWPrSSH9+6Oj0h25FWyQ\"}\",\"code\":200,\"message\":\"OK\"}");
        PowerMockito.suppress(PowerMockito.methods(AbstractHttpClient.class, "doPost"));

        Mockito.doReturn(responseMockedOK).when(testedClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());


        String merchantReqId = Calendar.getInstance().getTimeInMillis() + "007";
        OneyRefundRequest request = OneyRefundRequest.Builder.aOneyRefundRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withMerchantRequestId(merchantReqId)
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference("CMDE" + OneyConstants.PIPE + "455454545415451198119")
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withPurchase(PurchaseCancel.Builder.aPurchaseCancelBuilder()
                        .withReasonCode(1)
                        .withRefundFlag(true)
                        .withAmount(Float.valueOf("250"))
                        .build())
                .withCallParameters(params)
                .build();

        Assertions.assertNotNull(request);
        StringResponse transactStatus = testedClient.initiateRefundPayment(request);
        Assertions.assertNotNull(transactStatus.getCode());
        Assertions.assertNotNull(transactStatus.getContent());
    }
}
