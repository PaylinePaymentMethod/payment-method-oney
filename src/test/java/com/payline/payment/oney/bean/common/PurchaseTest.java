package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.utils.OneyConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.BeanUtils.createDelivery;
import static com.payline.payment.oney.utils.BeanUtils.createItemList;
import static com.payline.payment.oney.utils.TestUtils.createCompletePaymentRequestBuilder;

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

}
