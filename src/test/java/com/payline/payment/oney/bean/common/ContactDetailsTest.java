package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.customer.ContactDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.TestUtils.createDefaultBuyer;

public class ContactDetailsTest {

    private ContactDetails contactDetails;

    @Test
    public void fromPayline() throws Exception {

        contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                .fromPayline(createDefaultBuyer())
                .build();
        Assertions.assertNotNull(contactDetails.getLandLineNumber());
        Assertions.assertNotNull(contactDetails.getMobilePhoneNumber());
        Assertions.assertNotNull(contactDetails.getEmailAdress());
    }

    @Test
    public void testToString() throws Exception {
        contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                .withLandLineNumber("0436656565")
                .withMobilePhoneNumber("0636656565")
                .withEmailAdress("foo@bar.fr")
                .build();
        Assertions.assertTrue(contactDetails.toString().contains("landline_number"));
        Assertions.assertTrue(contactDetails.toString().contains("mobile_phone_number"));
        Assertions.assertTrue(contactDetails.toString().contains("email_address"));
    }

}
