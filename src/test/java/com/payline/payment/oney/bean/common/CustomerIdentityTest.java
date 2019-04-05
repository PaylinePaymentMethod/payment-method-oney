package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.customer.CustomerIdentity;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.common.Buyer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.TestUtils.createDefaultBuyer;

public class CustomerIdentityTest {

    @Test
    void customerFromPaylineTest() {
        Buyer buyer = TestUtils.createDefaultBuyer();
        CustomerIdentity customerIdentity = CustomerIdentity.Builder.aCustomerIdentity().fromPayline(buyer).build();
        Assertions.assertNull(customerIdentity.getLastName());
    }

    @Test
    public void customerIdentityTest() {
        CustomerIdentity customerIdentity = CustomerIdentity.Builder.aCustomerIdentity()
                .withBirthName("Doe")
                .withPersonType(2)
                .withHonorificCode(1)
                .withFirstName("John")
                //Optinals
                .withLastName("LN")
                .withGivenNames("GN")
                .withBirthDate("1990-12-11")
                .build();
        Assertions.assertNotNull(customerIdentity);
    }

    @Test
    public void fromPaylineBuyer() {
        Buyer buyer = createDefaultBuyer();
        CustomerIdentity customerIdentity = CustomerIdentity.Builder.aCustomerIdentity()
                .fromPayline(buyer)
                .build();
        Assertions.assertNotNull(customerIdentity.getFirstName());
        Assertions.assertNotNull(customerIdentity.getBirthDate());
        Assertions.assertNotNull(customerIdentity.getHonorificCode());
        Assertions.assertNotNull(customerIdentity.getPersonType());
        //Champs pas encore mappés
        Assertions.assertNull(customerIdentity.getGivenNames());
        Assertions.assertNull(customerIdentity.getBirthMunicipalityCode());
        Assertions.assertNull(customerIdentity.getCityzenshipCountryCode());
    }

    // PAYLAPMEXT-147
    @Test
    public void fromPaylineBuyer_noLegalStatus(){
        Buyer buyer = Buyer.BuyerBuilder.aBuyer()
                .withEmail(TestUtils.generateRamdomEmail())
                .withFullName(TestUtils.createFullName())
                .build();

        CustomerIdentity customerIdentity = CustomerIdentity.Builder.aCustomerIdentity()
                .fromPayline(buyer)
                .build();

        Assertions.assertNotNull(  customerIdentity.getPersonType() );
    }

    @Test
    public void testToString() {
        CustomerIdentity customerIdentity = CustomerIdentity.Builder.aCustomerIdentity()
                .withBirthName("Doe")
                .withPersonType(2)
                .withHonorificCode(1)
                .withFirstName("John")
                //Optinals
                .withLastName("LN")
                .withGivenNames("GN")
                .withBirthDate("1990-12-11")
                .withBirthCountryCode("FR")
                .withTaxpayerCode("34000")
                .withBirthMunicipalityCode("75018")
                .withBirthArrondissementCode(18)
                .withCitizenCountryCode("FRA")
                .withCompanyName("DKA")
                .build();

        Assertions.assertTrue(customerIdentity.toString().contains("individual_taxpayer_code"));
        Assertions.assertTrue(customerIdentity.toString().contains("person_type"));
        Assertions.assertTrue(customerIdentity.toString().contains("honorific_code"));
        Assertions.assertTrue(customerIdentity.toString().contains("birth_name"));
        Assertions.assertTrue(customerIdentity.toString().contains("last_name"));
        Assertions.assertTrue(customerIdentity.toString().contains("first_name"));
        Assertions.assertTrue(customerIdentity.toString().contains("given_names"));
        Assertions.assertTrue(customerIdentity.toString().contains("birth_date"));
        Assertions.assertTrue(customerIdentity.toString().contains("birth_municipality_code"));
        Assertions.assertTrue(customerIdentity.toString().contains("birth_country_code"));
        Assertions.assertTrue(customerIdentity.toString().contains("citizenship_country_code"));
        Assertions.assertTrue(customerIdentity.toString().contains("company_name"));
    }

}
