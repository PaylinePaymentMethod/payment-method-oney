package com.payline.payment.oney.common.bean;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.exception.InvalidRequestException;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.service.impl.BeanAssemblerServiceImpl;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NavigationDataTest {

    private NavigationData navigationData;

    private BeanAssembleService beanAssembleService;

    private PaymentRequest paymentRequest;

    @BeforeAll
    public void setUp() {
        beanAssembleService = BeanAssemblerServiceImpl.getInstance();
        paymentRequest = TestUtils.createDefaultPaymentRequest();
    }

    @Test
    public void buildNavigationData() {
        navigationData = NavigationData.Builder.aNavigationDataBuilder()
                .withFailUrl("fail/url/")
                .withSuccesUrl("success/url/")
                .withNotificationUrl("notification/url/")
                .withPendingUrl("pending/url/")
                .build();

        Assertions.assertEquals("fail/url/", navigationData.getFailUrl());
        Assertions.assertEquals("success/url/", navigationData.getSuccessUrl());
        Assertions.assertEquals("notification/url/", navigationData.getNotificationUrl());
        Assertions.assertEquals("pending/url/", navigationData.getPendingUrl());

    }

    @Test
    public void buildNavigationDataFail() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            navigationData = NavigationData.Builder.aNavigationDataBuilder()
                    .withFailUrl("fail/url/")
                    .withNotificationUrl("notification/url/")
                    .withPendingUrl("pending/url/")
                    .build();
        });
        Assertions.assertEquals("NavigationData must have a successUrl when built", exception.getMessage());


    }

    @Test
    public void buildNavigationDataFromPayline() throws InvalidRequestException {

        navigationData = beanAssembleService.assembleNavigationData(paymentRequest);

        Assertions.assertEquals("https://succesurl.com/", navigationData.getNotificationUrl());
        Assertions.assertEquals("http://redirectionURL.com", navigationData.getPendingUrl());
        Assertions.assertEquals("http://redirectionURL.com", navigationData.getSuccessUrl());
        Assertions.assertEquals("http://redirectionCancelURL.com", navigationData.getFailUrl());
    }


    @Test
    public void toStringTest() throws InvalidRequestException {

        navigationData = beanAssembleService.assembleNavigationData(paymentRequest);

        Assertions.assertTrue(navigationData.toString().contains("success_url"));
        Assertions.assertTrue(navigationData.toString().contains("fail_url"));
        Assertions.assertTrue(navigationData.toString().contains("server_response_url"));
        Assertions.assertTrue(navigationData.toString().contains("alternative_return_url"));

    }
}
