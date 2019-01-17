package com.payline.payment.oney.common.bean;

import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.utils.PluginUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;

import static com.payline.payment.oney.utils.BeanUtils.*;
import static com.payline.payment.oney.utils.TestUtils.CONFIRM_AMOUNT;
import static com.payline.payment.oney.utils.TestUtils.createCompletePaymentBuilder;

public class PurchaseTest {
    private Purchase purchase;

    @Test
    public void purchase() {
        purchase = Purchase.Builder.aPurchaseBuilder()
                .withPurchaseMerchant(createPurchaseMerchant())
                .withCurrencyCode("EUR")
                .withPurchaseAmount(150f)
                .withDelivery(createDelivery())
                .withExternalReference("externalRef")
                .withListItem(createItemList())
                .withNumberOfItems(2)
                .withExternalReferenceType("CMDE")
                .build();

        Assertions.assertNotNull(purchase);
    }


    @Test
    public void fromPaymentRequest() {
        purchase = Purchase.Builder.aPurchaseBuilder()
                .fromPayline(createCompletePaymentBuilder().build())
                .build();

        Float paymentAmountConverted  = PluginUtils.createFloatAmount(new BigInteger(CONFIRM_AMOUNT), Currency.getInstance("EUR"));
        Assertions.assertEquals(paymentAmountConverted, purchase.getPurchaseAmount(),0.01);

        //Test to String here
        Assertions.assertTrue(purchase.toString().contains("external_reference"));
        Assertions.assertTrue(purchase.toString().contains("purchase_amount"));
        Assertions.assertTrue(purchase.toString().contains("currency_code"));
        Assertions.assertTrue(purchase.toString().contains("delivery"));
        Assertions.assertTrue(purchase.toString().contains("item_list"));
    }

    @Test
    public void withoutListItem() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            purchase = Purchase.Builder.aPurchaseBuilder()
                    .withPurchaseMerchant(createPurchaseMerchant())
                    .withCurrencyCode("EUR")
                    .withPurchaseAmount(150f)
                    .withDelivery(createDelivery())
                    .withExternalReference("externalRef")
                    .withNumberOfItems(2)
                    .withExternalReferenceType("CMDE")
                    .build();
        });
        Assertions.assertEquals("Purchase must have a listItem when built", exception.getMessage());


    }

    @Test
    public void withoutDelivery() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            purchase = Purchase.Builder.aPurchaseBuilder()
                    .withPurchaseMerchant(createPurchaseMerchant())
                    .withCurrencyCode("EUR")
                    .withPurchaseAmount(150f)
                    .withExternalReference("externalRef")
                    .withListItem(createItemList())
                    .withNumberOfItems(2)
                    .withExternalReferenceType("CMDE")
                    .build();
        });
        Assertions.assertEquals("Purchase must have a delivery when built", exception.getMessage());


    }

    @Test
    public void withoutExternalReferenceType() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            purchase = Purchase.Builder.aPurchaseBuilder()
                    .withPurchaseMerchant(createPurchaseMerchant())
                    .withCurrencyCode("EUR")
                    .withPurchaseAmount(150f)
                    .withDelivery(createDelivery())
                    .withExternalReference("externalRef")
                    .withListItem(createItemList())
                    .withNumberOfItems(2)
                    .build();
        });
        Assertions.assertEquals("Purchase must have a externalReferenceType when built", exception.getMessage());


    }

    @Test
    public void withoutExternalReference() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            purchase = Purchase.Builder.aPurchaseBuilder()
                    .withPurchaseMerchant(createPurchaseMerchant())
                    .withCurrencyCode("EUR")
                    .withPurchaseAmount(150f)
                    .withDelivery(createDelivery())
                    .withExternalReferenceType("CMDE")
                    .withListItem(createItemList())
                    .withNumberOfItems(2)
                    .build();
        });
        Assertions.assertEquals("Purchase must have a externalReference when built", exception.getMessage());


    }

    @Test
    public void withoutPuchaseAmount() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            purchase = Purchase.Builder.aPurchaseBuilder()
                    .withPurchaseMerchant(createPurchaseMerchant())
                    .withCurrencyCode("EUR")
                    .withDelivery(createDelivery())
                    .withExternalReference("externalRef")
                    .withListItem(createItemList())
                    .withNumberOfItems(2)
                    .withExternalReferenceType("CMDE")
                    .build();
        });
        Assertions.assertEquals("Purchase must have a purchaseAmount when built", exception.getMessage());


    }

    @Test
    public void withoutCurrencyCode() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            purchase = Purchase.Builder.aPurchaseBuilder()
                    .withPurchaseMerchant(createPurchaseMerchant())
                    .withPurchaseAmount(150f)
                    .withDelivery(createDelivery())
                    .withExternalReference("externalRef")
                    .withListItem(createItemList())
                    .withNumberOfItems(2)
                    .withExternalReferenceType("CMDE")
                    .build();
        });
        Assertions.assertEquals("Purchase must have a currencyCode when built", exception.getMessage());


    }

}
