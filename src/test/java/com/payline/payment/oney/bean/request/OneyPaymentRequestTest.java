package com.payline.payment.oney.bean.request;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.service.impl.BeanAssemblerServiceImpl;
import com.payline.payment.oney.service.impl.RequestConfigServiceImpl;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OneyPaymentRequestTest {

    private OneyPaymentRequest oneyPaymentRequest;

    private BeanAssembleService beanAssembleService;

    private PaymentRequest paymentRequest;

    private String merchGuid;
    private String language;
    private String merchantRequestId;
    private String pspGuid;
    private String chiffrementKey;
    private BusinessTransactionData businessTransaction;
    private PaymentData paymentData;
    private NavigationData navigationData;
    private Customer customer;
    private Purchase purchase;

    @BeforeAll
    void setUp() throws Exception {
        beanAssembleService = BeanAssemblerServiceImpl.getInstance();
        paymentRequest = TestUtils.createDefaultPaymentRequest();
        merchGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, MERCHANT_GUID_KEY);
        language = paymentRequest.getLocale().getLanguage();
        merchantRequestId = PluginUtils.generateMerchantRequestId(merchGuid);
        pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PSP_GUID_KEY);
        chiffrementKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PARTNER_CHIFFREMENT_KEY);
        businessTransaction = beanAssembleService.assembleBuisnessTransactionData(paymentRequest);
        paymentData = beanAssembleService.assemblePaymentData(paymentRequest, businessTransaction);
        navigationData = beanAssembleService.assembleNavigationData(paymentRequest);
        customer = beanAssembleService.assembleCustomer(paymentRequest);
        purchase = beanAssembleService.assemblePurchase(paymentRequest);
    }

    @Test
    void buildOneyPaymentRequest() throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("test", "test");

        oneyPaymentRequest = OneyPaymentRequest.Builder.aOneyPaymentRequest()
                .withLanguageCode(language)
                .withMerchantRequestId(merchantRequestId)
                .withPspGuid(pspGuid)
                .withMerchantGuid(merchGuid)
                .withNavigation(navigationData)
                .withPaymentdata(paymentData)
                .withCustomer(customer)
                .withPurchase(purchase)
                .withMerchantLanguageCode(language)
                .withEncryptKey(chiffrementKey)
                .withMerchantContext(paymentRequest.getSoftDescriptor())
                .withPspContext(paymentRequest.getTransactionId())
                .withCallParameters(map)
                .build();


        String result = oneyPaymentRequest.toString();
        Assertions.assertTrue(result.contains("merchant_guid"));
        Assertions.assertTrue(result.contains("psp_guid"));
        Assertions.assertTrue(result.contains("language_code"));
        Assertions.assertTrue(result.contains("merchant_request_id"));
        Assertions.assertTrue(result.contains("purchase"));
        Assertions.assertTrue(result.contains("customer"));
        Assertions.assertTrue(result.contains("payment"));
    }
}



