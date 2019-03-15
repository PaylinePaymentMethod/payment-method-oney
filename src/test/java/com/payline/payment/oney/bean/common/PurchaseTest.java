package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;

import static com.payline.payment.oney.utils.BeanUtils.createDelivery;
import static com.payline.payment.oney.utils.BeanUtils.createItemList;
import static com.payline.payment.oney.utils.TestUtils.CONFIRM_AMOUNT;
import static com.payline.payment.oney.utils.TestUtils.createCompletePaymentBuilder;

public class PurchaseTest {
    private Purchase purchase;

    @Test
    public void purchase() throws Exception {
        purchase = Purchase.Builder.aPurchaseBuilder()
                .withCurrencyCode("EUR")
                .withPurchaseAmount(150f)
                .withDelivery(createDelivery())
                .withExternalReference("externalRef")
                .withListItem(createItemList())
                .withNumberOfItems(2)
                .withExternalReferenceType(OneyConstants.EXTERNAL_REFERENCE_TYPE)
                .build();

        Assertions.assertNotNull(purchase);
    }

    @Test
    public void fromPaymentRequest() throws Exception {
        purchase = Purchase.Builder.aPurchaseBuilder()
                .fromPayline(createCompletePaymentBuilder().build())
                .build();

        Float paymentAmountConverted = PluginUtils.createFloatAmount(new BigInteger(CONFIRM_AMOUNT), Currency.getInstance("EUR"));
        Assertions.assertEquals(paymentAmountConverted, purchase.getPurchaseAmount(), 0.01);

        //Test to String here
        Assertions.assertTrue(purchase.toString().contains("external_reference"));
        Assertions.assertTrue(purchase.toString().contains("purchase_amount"));
        Assertions.assertTrue(purchase.toString().contains("currency_code"));
        Assertions.assertTrue(purchase.toString().contains("delivery"));
        Assertions.assertTrue(purchase.toString().contains("item_list"));
    }

}
