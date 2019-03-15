package com.payline.payment.oney.bean.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PurchaseCancelTest {

    private PurchaseCancel purchaseCancel;


    @Test
    public void testPurchaseCancel() throws Exception {
        purchaseCancel = PurchaseCancel.Builder.aPurchaseCancelBuilder()
                .withRefundFlag(true)
                .withReasonCode(0)
                .withAmount(Float.valueOf("25"))
                .build();

        Assertions.assertNotNull(purchaseCancel);
    }


    @Test
    public void testPurchaseCancel1() throws Exception {
        purchaseCancel = PurchaseCancel.Builder.aPurchaseCancelBuilder()
                .withRefundFlag(true)
                .withReasonCode(1)
                .build();

        Assertions.assertNotNull(purchaseCancel);
    }

    @Test
    public void testToString() throws Exception {
        purchaseCancel = PurchaseCancel.Builder.aPurchaseCancelBuilder()
                .withRefundFlag(true)
                .withReasonCode(0)
                .withAmount(Float.valueOf("25"))
                .build();

        Assertions.assertTrue(purchaseCancel.toString().contains("refund_down_payment"));
        Assertions.assertTrue(purchaseCancel.toString().contains("cancellation_reason_code"));
        Assertions.assertTrue(purchaseCancel.toString().contains("cancellation_amount"));
    }
}
