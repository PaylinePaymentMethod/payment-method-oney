package com.payline.payment.oney.common.bean;

import com.payline.payment.oney.bean.common.customer.ContactDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.TestUtils.createDefaultBuyer;

public class ContactDetailsTest {

    private ContactDetails contactDetails;


    @Test
    public void contactDetails() {
        contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                .withLandLineNumber("0436656565")
                .withMobilePhoneNumber("0636656565")
                .withEmailAdress("foo@bar.fr")
                .build();
        Assertions.assertNotNull(contactDetails.getLandLineNumber());
        Assertions.assertNotNull(contactDetails.getMobilePhoneNumber());
    }

    @Test
    public void withoutLandlineNumber() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {

            contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                    .withMobilePhoneNumber("0636656565")
                    .withEmailAdress("foo@bar.fr")
                    .build();

        });
        Assertions.assertEquals("ContactDetails must have a landLineNumber when built", exception.getMessage());

    }

    @Test
    public void withoutMobilePhoneNumber() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                    .withLandLineNumber("0436656565")
                    .withEmailAdress("foo@bar.fr")
                    .build();

        });
        Assertions.assertEquals("ContactDetails must have a mobilePhoneNumber when built", exception.getMessage());

    }

    @Test
    public void withoutEmail() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {

            contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                    .withLandLineNumber("0436656565")
                    .withMobilePhoneNumber("0636656565")
                    .build();

        });
        Assertions.assertEquals("ContactDetails must have a  valid emailAddress when built", exception.getMessage());

    }


    @Test
    public void fromPayline() {

        contactDetails = ContactDetails.Builder.aContactDetailsBuilder()
                .fromPayline(createDefaultBuyer())
                .build();
        Assertions.assertNotNull(contactDetails.getLandLineNumber());
        Assertions.assertNotNull(contactDetails.getMobilePhoneNumber());
        Assertions.assertNotNull(contactDetails.getEmailAdress());
    }

    @Test
    public void testToString() {
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
