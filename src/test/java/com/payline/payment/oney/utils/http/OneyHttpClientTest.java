package com.payline.payment.oney.utils.http;

import com.payline.payment.oney.bean.common.PurchaseCancel;
import com.payline.payment.oney.bean.request.OneyRefundRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpVersion;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.Configurable;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.PARTNER_API_URL;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PrepareForTest(AbstractHttpClient.class)
public class OneyHttpClientTest {


    //ToDO  mock http call, not mocked now to check if they work
    @Spy
    @InjectMocks
    OneyHttpClient testedClient;

    @Mock
    CloseableHttpClient closableClient;

    private Map<String, String> params;
    private Map<String, String> urlParams;
    private static HashMap<String, String> partnerConfigurationMap;
    private static RequestConfig requestConfig;


    @BeforeEach
    public void setup() {
        partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(OneyHttpClient.KEY_CONNECT_TIMEOUT,"2000");
        partnerConfigurationMap.put(OneyHttpClient.CONNECTION_REQUEST_TIMEOUT,"3000");
        partnerConfigurationMap.put(OneyHttpClient.READ_SOCKET_TIMEOUT,"4000");

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(2000)
                .setConnectionRequestTimeout(3000)
                .setSocketTimeout(4000).build();

        testedClient = OneyHttpClient.getInstance(new PartnerConfiguration(partnerConfigurationMap, new HashMap<>()));
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(testedClient, "client", closableClient);

        params = new HashMap<>();
        params.put("psp_guid", "6ba2a5e2-df17-4ad7-8406-6a9fc488a60a");
        params.put("merchant_guid", "9813e3ff-c365-43f2-8dca-94b850befbf9");
        params.put("reference", URLEncoder.encode(PluginUtils.fullPurchaseReference("455454545415451198a")));
        params.put(PARTNER_API_URL, "https://oney-staging.azure-api.net");

        urlParams = new HashMap<>();
        urlParams.put( OneyHttpClient.LANGUAGE_CODE, "fr" );
    }

    @Test
    public void doGet() throws Exception {

        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, 200, "FINE!"));
        Mockito.when(entity.getContent()).thenReturn(getClass().getClassLoader().getResourceAsStream("result.txt"));
        Mockito.when(httpResponse.getEntity()).thenReturn(entity);
        Mockito.doReturn(httpResponse).when(closableClient).execute(Mockito.any());

        StringResponse response = testedClient.doGet("/staging/payments/v1/purchase/", params, urlParams);

        //Assert we have a response
        assertNotNull(response);
        assertEquals(200, response.getCode());

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

        StringResponse response = testedClient.doPost(path, requestContent, params);

        //Assert we have a response
        assertNotNull(response);
        assertEquals(400, response.getCode());

    }

    @Test
    public void buildGetOrderPath() {


        Map<String, String> param = new HashMap<>();
        param.put("psp_guid", "val1");
        param.put("merchant_guid", "val2");
        param.put("reference", "val3");

        String pathAttempted = "somePath/psp_guid/val1/merchant_guid/val2/reference/val3";
        String path = testedClient.buildGetOrderPath("somePath", param);
        assertEquals(pathAttempted, path);
    }

    @Test
    public void buildConfirmOrderPath() {

        Map<String, String> param = new HashMap<>();
        param.put("psp_guid", "val1");
        param.put("merchant_guid", "val2");
        param.put("reference", "val3");

        String pathAttempted = "somePath/psp_guid/val1/merchant_guid/val2/reference/val3/action/confirm";
        String path = testedClient.buildConfirmOrderPath("somePath", param);
        assertEquals(pathAttempted, path);
    }

    @Test
    public void initiateGetTransactionStatusTest() throws Exception {

        StringResponse responseMockedOK = createStringResponse(200, "ZZOK", "{\"content\":\"{\\\"encrypted_message\\\":\\\"+l2i0o7hGRh+wJO02++ul41+5xLG5BBT+jV4I19n1BxNgTTBkgClTslC3pM/0UXrEOJt3Nv3LTMrGFG1pzsOP6gxM5c+lw57K0YUbQqoGgI\\u003d\\\"}\",\"code\":200,\"message\":\"OK\"}");
        PowerMockito.suppress(PowerMockito.methods(AbstractHttpClient.class, "doGet"));

        Mockito.doReturn(responseMockedOK).when(testedClient).doGet(Mockito.anyString(), Mockito.anyMap(), Mockito.anyMap());


        OneyTransactionStatusRequest request = OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference(PluginUtils.fullPurchaseReference("455454545415451198114"))
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withCallParameters(params)
                .build();

        assertNotNull(request);
        StringResponse transactStatus = testedClient.initiateGetTransactionStatus(request, true);

        assertEquals(200,transactStatus.getCode());
    }


    @Test
    public void initiateRefundRequestTest() throws Exception {

        StringResponse responseMockedOK = createStringResponse(200, "OK", "{\"content\":\"{\\\"encrypted_message\\\":\\\"+l2i0o7hGRh+wJO02++ul+pupX40ZlQGwcgL91laJl8Vmw5MnvB6zm+cpQviUjey0a4YEoiRButKTLyhHS8SBlDyClrx8GM0AWSp0+DsthbblWPrSSH9+6Oj0h25FWyQ\"}\",\"code\":200,\"message\":\"OK\"}");
        PowerMockito.suppress(PowerMockito.methods(AbstractHttpClient.class, "doPost"));

        Mockito.doReturn(responseMockedOK).when(testedClient).doPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());


        String merchantReqId = Calendar.getInstance().getTimeInMillis() + "007";
        OneyRefundRequest request = OneyRefundRequest.Builder.aOneyRefundRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withMerchantRequestId(merchantReqId)
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference(PluginUtils.fullPurchaseReference("455454545415451198119"))
                .withEncryptKey("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=")
                .withPurchase(PurchaseCancel.Builder.aPurchaseCancelBuilder()
                        .withReasonCode(1)
                        .withRefundFlag(true)
                        .withAmount(Float.valueOf("250"))
                        .build())
                .withCallParameters(params)
                .build();

        assertNotNull(request);
        StringResponse transactStatus = testedClient.initiateRefundPayment(request, true);

        assertEquals(200,transactStatus.getCode());
        assertNotNull(transactStatus.getContent());
    }

    @Test
    public void finalPath_sandbox(){
        String finalPath = testedClient.finalPath( "/path", true );
        assertTrue( finalPath.startsWith( OneyConstants.SANDBOX_PATH_PREFIX ) );
    }

    @Test
    public void finalPath_prod(){
        String finalPath = testedClient.finalPath( "/path", false );
        assertEquals( "/path", finalPath );
    }


    @Test
    public void testWithNoPoolMaxSize() throws Exception {
        getHttpClient("30000", "30000", "3", null, "400000");
    }

    @Test
    public void testWithEmptyPoolMaxSize() throws Exception {
        getHttpClient("30000", "30000", "3", "", "400000");
    }


    @Test
    public void testWithNoPoolValidation() throws Exception{
        getHttpClient("30000", "30000", "3", "360000", null);
    }

    @Test
    public void testWithEmptyPoolValidation() throws Exception {
        getHttpClient(null, null,null,null,"");
    }

    @Test
    public void testWithAllOptions() throws Exception {
        getHttpClient("30000", "30000", "3", "360000", "400000");
    }

    @Test
    public void testWithEmptyOptions() throws Exception {
        getHttpClient("", "", "", "", "");
    }


    private void getHttpClient(final String connectionTimeToLive, final String evictIdleConnectionTimeout, final String keepAliveDuration,
                               final String poolMaxSize, final String poolValidate) throws IOException {
        HashMap<String, String> partnerMapTest = getParametersMap(connectionTimeToLive, evictIdleConnectionTimeout, keepAliveDuration,
                poolMaxSize, poolValidate);
        final PartnerConfiguration partnerConfiguration = new PartnerConfiguration(partnerMapTest, new HashMap<>());
        HttpClientBuilder builder = testedClient.getHttpClientBuilder(partnerConfiguration, requestConfig);
        assertNotNull(builder);
        try (CloseableHttpClient httpClient = builder.build()){
            RequestConfig requestConfig = ((Configurable) httpClient).getConfig();
            assertEquals(4000, requestConfig.getSocketTimeout());
            assertEquals(3000, requestConfig.getConnectionRequestTimeout());
            assertEquals(2000, requestConfig.getConnectTimeout());
        }

    }


    private HashMap<String, String> getParametersMap(final String connectionTimeToLive, final String evictIdleConnectionTimeout, final String keepAliveDuration, final String poolMaxSize, final String poolValidate) {
        HashMap<String, String> partnerMapTest = new HashMap<>(partnerConfigurationMap);
        partnerMapTest.put(OneyHttpClient.CONNECTION_TIME_TO_LIVE, connectionTimeToLive);
        partnerMapTest.put(OneyHttpClient.EVICT_IDLE_CONNECTION_TIMEOUT, evictIdleConnectionTimeout);
        partnerMapTest.put(OneyHttpClient.KEEP_ALIVE_DURATION, keepAliveDuration);
        partnerMapTest.put(OneyHttpClient.POOL_MAX_SIZE_PER_ROUTE, poolMaxSize);
        partnerMapTest.put(OneyHttpClient.POOL_VALIDATE_CONN_AFTER_INACTIVITY, poolValidate);
        return partnerMapTest;
    }


}
