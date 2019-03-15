package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.purchase.Delivery;
import com.payline.pmapi.bean.common.Buyer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.TestUtils.createDefaultBuyer;

public class DeliveryTest {


    private Delivery delivery;


    @Test
    public void testDelivery() throws Exception {
        delivery = Delivery.Builder.aDeliveryBuilder()
                .withDeliveryDate("1998-07-12")
                .withDeliveryModeCode(1)
                .withDeliveryOption(1)
                .withAddressType(1)
                .withRecipient(null)
                .withDeliveryAddress(OneyAddress.Builder.aOneyAddressBuilder()
                        .fromPayline(createDefaultBuyer(), Buyer.AddressType.DELIVERY)
                        .build())
                .build();

        Assertions.assertNotNull(delivery.getAddressType());
        Assertions.assertNotNull(delivery.getDeliveryDate());
        Assertions.assertNotNull(delivery.getDeliveryOption());
        Assertions.assertNotNull(delivery.getDeliveryModeCode());
        Assertions.assertNotNull(delivery.getDeliveryAddress());
    }

    @Test
    public void testToString() throws Exception {
        delivery = Delivery.Builder.aDeliveryBuilder()
                .withDeliveryDate("1998-07-12")
                .withDeliveryModeCode(1)
                .withDeliveryOption(1)
                .withAddressType(1)
                .withDeliveryAddress(OneyAddress.Builder.aOneyAddressBuilder()
                        .fromPayline(createDefaultBuyer(), Buyer.AddressType.DELIVERY)
                        .build())
                .build();
        Assertions.assertTrue(delivery.toString().contains("delivery_date"));
        Assertions.assertTrue(delivery.toString().contains("delivery_mode_code"));
        Assertions.assertTrue(delivery.toString().contains("delivery_option"));
        Assertions.assertTrue(delivery.toString().contains("address_type"));
        Assertions.assertTrue(delivery.toString().contains("delivery_address"));

    }
}
