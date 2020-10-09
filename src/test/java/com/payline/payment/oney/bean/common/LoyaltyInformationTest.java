package com.payline.payment.oney.bean.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LoyaltyInformationTest {

    private LoyaltyInformation loyaltyInformation;


    @Test
    void loyaltyInformationOK() {
        loyaltyInformation = LoyaltyInformation.Builder.aLoyaltyInformationBuilder()
                .withLoyaltyId("1")
                .withType("mytype")
                .withLoyatyFaqUrl("some/url")
                .withExpirationDate("2018-04-24")
                .withValue("25")
                .build();
        Assertions.assertEquals("1", loyaltyInformation.getLoyaltyId());
        Assertions.assertEquals("mytype", loyaltyInformation.getType());
        Assertions.assertEquals("some/url", loyaltyInformation.getLoyaltyFaqUrl());
        Assertions.assertEquals("2018-04-24", loyaltyInformation.getExpirationDate());
        Assertions.assertEquals("25", loyaltyInformation.getValue());
    }

    @Test
    void testToString() {
        loyaltyInformation = LoyaltyInformation.Builder.aLoyaltyInformationBuilder()
                .withLoyaltyId("1")
                .withType("mytype")
                .withLoyatyFaqUrl("some/url")
                .withExpirationDate("2018-04-24")
                .withValue("25")
                .build();
        Assertions.assertTrue(loyaltyInformation.toString().contains("loyalty_id"));
        Assertions.assertTrue(loyaltyInformation.toString().contains("loyalty_faq_url"));
        Assertions.assertTrue(loyaltyInformation.toString().contains("expiration_date"));
        Assertions.assertTrue(loyaltyInformation.toString().contains("type"));
        Assertions.assertTrue(loyaltyInformation.toString().contains("value"));
    }
}
