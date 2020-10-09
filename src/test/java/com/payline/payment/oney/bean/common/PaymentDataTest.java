package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.enums.PaymentType;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.service.impl.BeanAssemblerServiceImpl;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.math.BigInteger;
import java.util.Currency;

import static com.payline.payment.oney.utils.BeanUtils.createDefaultBusinessTransactionData;
import static com.payline.payment.oney.utils.TestUtils.CONFIRM_AMOUNT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentDataTest {

    private PaymentData paymentdata;

    private BeanAssembleService beanAssembleService;

    private PaymentRequest paymentRequest;


    @BeforeAll
    void setUp() {
        beanAssembleService = BeanAssemblerServiceImpl.getInstance();
        paymentRequest = TestUtils.createDefaultPaymentRequest();
    }

    @Test
    void paymentDataOK() throws Exception {
        paymentdata = PaymentData.Builder.aPaymentData()
                .withCurrency("EUR")
                .withAmount(100)
                .withBusinessTransactionList(createDefaultBusinessTransactionData("254"))
                .build();

        Assertions.assertEquals(100, paymentdata.getAmount(), 0.001);
        Assertions.assertEquals("EUR", paymentdata.getCurrency());
    }

    @Test
    void paymentDataFromPayline() throws Exception {
        final BusinessTransactionData businessTransaction = beanAssembleService.assembleBuisnessTransactionData(paymentRequest);
        paymentdata = beanAssembleService.assemblePaymentData(paymentRequest, businessTransaction);
//        paymentdata = PaymentData.Builder.aPaymentData()
//                .fromPayline(createCompletePaymentBuilder().build())
//                .build();

//conversion de l'amount de centimes d'euros  à euros
        Float paymentAmountConverted = PluginUtils.createFloatAmount(new BigInteger(CONFIRM_AMOUNT), Currency.getInstance("EUR"));
        Assertions.assertEquals(paymentAmountConverted, paymentdata.getAmount(), 0.01);
        Assertions.assertEquals("EUR", paymentdata.getCurrency());
    }

    @Test
    void toStringTest() throws Exception {
        paymentdata = PaymentData.Builder.aPaymentData()
                .withAmount(100)
                .withCurrency("EUR")
                .withPaymentType(PaymentType.DEFERRED.getValue())
                .withBusinessTransactionList(createDefaultBusinessTransactionData("254"))
                .build();

        Assertions.assertTrue(paymentdata.toString().contains("payment_amount"));
        Assertions.assertTrue(paymentdata.toString().contains("currency_code"));
        Assertions.assertTrue(paymentdata.toString().contains("payment_type"));
        Assertions.assertTrue(paymentdata.toString().contains("business_transaction"));
    }

}
