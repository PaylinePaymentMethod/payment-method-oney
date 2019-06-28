package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.capture.response.CaptureResponse;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseFailure;
import com.payline.pmapi.bean.capture.response.impl.CaptureResponseSuccess;
import com.payline.pmapi.service.CaptureService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static com.payline.payment.oney.utils.TestUtils.createStringResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CaptureServiceImplTest {

    @InjectMocks
    CaptureService service;

    @Spy
    OneyHttpClient client;

    @BeforeAll
    public void setup() {
        service = new CaptureServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void captureRequest() throws Exception {
        StringResponse responseMockedGet = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FAVORABLE\",\"status_label\":\"a label\"}}");
        StringResponse responseMockedConfirm = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"FUNDED\",\"status_label\":\"a label\"}}");

        Mockito.doReturn(responseMockedGet).when(client).initiateGetTransactionStatus(any(), anyBoolean());
        Mockito.doReturn(responseMockedConfirm).when(client).initiateConfirmationPayment(any(), anyBoolean());

        CaptureRequest request = TestUtils.createDefaultCaptureRequest();

        CaptureResponse response = service.captureRequest(request);
        Assertions.assertEquals(CaptureResponseSuccess.class, response.getClass());
    }

    @Test
    void captureRequestError() throws Exception{
        StringResponse responseMocked = createStringResponse(200, "OK", "{\"purchase\":{\"status_code\":\"REFUSED\",\"status_label\":\"a label\"}}");

        Mockito.doReturn(responseMocked).when(client).initiateGetTransactionStatus(any(), anyBoolean());
        Mockito.doReturn(responseMocked).when(client).initiateConfirmationPayment(any(), anyBoolean());

        CaptureRequest request = TestUtils.createDefaultCaptureRequest();

        CaptureResponse response = service.captureRequest(request);
        Assertions.assertEquals(CaptureResponseFailure.class, response.getClass());
    }

    @Test
    void captureRequestException() throws Exception{
        Mockito.doThrow(new HttpCallException("foo", "bar")).when(client).initiateGetTransactionStatus(any(), anyBoolean());

        CaptureRequest request = TestUtils.createDefaultCaptureRequest();

        CaptureResponse response = service.captureRequest(request);
        Assertions.assertEquals(CaptureResponseFailure.class, response.getClass());
    }

}
