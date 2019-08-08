package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.customer.PurchaseHistory;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.BuyerExtended;
import com.payline.pmapi.bean.payment.BuyerExtendedHistory;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;
import java.util.Date;

public class BeanAssemblerServiceImplTest {
    private BeanAssembleService beanAssembleService = BeanAssemblerServiceImpl.getInstance();


    @Test
    public void assemblePurchaseHistoryFull(){
        BuyerExtendedHistory buyerExtendedHistory = BuyerExtendedHistory.BuyerExtendedHistoryBuilder.aBuyerExtendedHistory()
                .withFirstOrderDate(new Date())
                .withLastOrderDate(new Date())
                .withOrderCount6Months(10)
                .build();

        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(TestUtils.generateRamdomEmail())
                .withPhoneNumbers(null)
                .withAddresses(null)
                .withFullName(null)
                .withCustomerIdentifier("a customer")
                .withExtendedData(TestUtils.createDefaultExtendedData())
                .withBirthday(null)
                .withLegalStatus(Buyer.LegalStatus.PERSON)
                .withBuyerExtendedHistory(buyerExtendedHistory)
                .withAccountAverageAmount(TestUtils.createAmount("10", Currency.getInstance("EUR")))
                .withAccountOrderCount(BigInteger.TEN)
                .build();

        // create paymentRequest
        PaymentRequest request = TestUtils.createCompletePaymentBuilder()
                .withBuyer(buyer)
                .build();

        // test method
        PurchaseHistory purchaseHistory = beanAssembleService.assemblePurchaseHistory(request);

        Assertions.assertEquals(Integer.valueOf(10),purchaseHistory.getNumberOfPurchase());
        Assertions.assertEquals(Float.valueOf(100),purchaseHistory.getTotalAmount());
        Assertions.assertNotNull(purchaseHistory.getFirstPurchasedate());
        Assertions.assertNotNull(purchaseHistory.getLastPurchaseDate());
    }

    @Test
    public void assemblePurchaseHistoryNoExtended(){
        // create paymentRequest
        PaymentRequest request = TestUtils.createCompletePaymentBuilder().build();

        // test method
        PurchaseHistory purchaseHistory = beanAssembleService.assemblePurchaseHistory(request);

        Assertions.assertEquals(Integer.valueOf(0),purchaseHistory.getNumberOfPurchase());
        Assertions.assertEquals(Float.valueOf(0),purchaseHistory.getTotalAmount());
        Assertions.assertNull(purchaseHistory.getFirstPurchasedate());
        Assertions.assertNull(purchaseHistory.getLastPurchaseDate());
    }

}
