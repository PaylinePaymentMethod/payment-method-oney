package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.utils.TestCountry;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.pmapi.bean.common.Buyer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.TestUtils.createDefaultBuyer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OneyAddressTest {

    private OneyAddress oneyAddress;

    private Map<Buyer.PhoneNumberType, String> phoneNumbers;

    private Map<Buyer.AddressType, Buyer.Address> addresses;

    @BeforeAll
    void setUp() {
        phoneNumbers = new HashMap<>();
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, TestUtils.getTestphoneNumber(TestCountry.IT));

        addresses = new HashMap<>();
    }

    @AfterEach
    void tearDown() {
        addresses.clear();
    }

    @Test
    void customerAddressOK() {
        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .fromPayline(createDefaultBuyer(), Buyer.AddressType.BILLING)
                .build();

        Assertions.assertNotNull(oneyAddress);
        Assertions.assertTrue(oneyAddress.getLine1().length() < 39);
        Assertions.assertTrue(oneyAddress.getLine2() == null || oneyAddress.getLine2().length() < 39);
        Assertions.assertTrue(oneyAddress.getLine3() == null || oneyAddress.getLine3().length() < 39);
        Assertions.assertTrue(oneyAddress.getLine4() == null || oneyAddress.getLine4().length() < 39);
        Assertions.assertTrue(oneyAddress.getLine5() == null || oneyAddress.getLine5().length() < 39);
    }

    @Test
    void testToString() {

        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .withLine1("12 place de la Comedie")
                .withLine2("residence ABC")
                .withLine3("bat D")
                .withLine4("etage E")
                .withLine5("porte F")
                .withCountryCode("FRA")
                .withCountryLabel("France")
                .withPostalCode("34000")
                .withMunicipality("mtp")
                .build();
        Assertions.assertTrue(oneyAddress.toString().contains("line1"));
        Assertions.assertTrue(oneyAddress.toString().contains("line2"));
        Assertions.assertTrue(oneyAddress.toString().contains("line3"));
        Assertions.assertTrue(oneyAddress.toString().contains("line4"));
        Assertions.assertTrue(oneyAddress.toString().contains("line5"));
        Assertions.assertTrue(oneyAddress.toString().contains("postal_code"));
        Assertions.assertTrue(oneyAddress.toString().contains("municipality"));
        Assertions.assertTrue(oneyAddress.toString().contains("country_code"));
        Assertions.assertTrue(oneyAddress.toString().contains("country_label"));


    }

    @Test
    void PAYLAPMEXT_101a() throws Exception {
        String street1 = "Viale BARBARO DI SAN GIORGIO RAMIRO 10";
        String street2 = "";

        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .fromPayline(getBuyer(street1, street2), Buyer.AddressType.BILLING)
                .build();

        Assertions.assertEquals("Viale BARBARO DI SAN GIORGIO RAMIRO 10", oneyAddress.getLine1());
        Assertions.assertNull(oneyAddress.getLine2());
        Assertions.assertNull(oneyAddress.getLine3());
        Assertions.assertNull(oneyAddress.getLine4());
        Assertions.assertNull(oneyAddress.getLine5());

    }

    @Test
    void PAYLAPMEXT_101b() throws Exception {
        String street1 = "Name of the pick up or store";
        String street2 = "Viale BARBARO DI SAN GIORGIO RAMIRO 10";


        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .fromPayline(getBuyer(street1, street2), Buyer.AddressType.BILLING)
                .build();

        Assertions.assertEquals("Name of the pick up or store Viale", oneyAddress.getLine1());
        Assertions.assertEquals("BARBARO DI SAN GIORGIO RAMIRO 10", oneyAddress.getLine2());
        Assertions.assertNull(oneyAddress.getLine3());
        Assertions.assertNull(oneyAddress.getLine4());
        Assertions.assertNull(oneyAddress.getLine5());

    }


    @Test
    void street_woBlank() throws Exception {
        String street1 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABBBBBBB";
        String street2 = null;


        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .fromPayline(getBuyer(street1, street2), Buyer.AddressType.BILLING)
                .build();

        Assertions.assertEquals("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", oneyAddress.getLine1());
        Assertions.assertEquals("BBBBBBB", oneyAddress.getLine2());
        Assertions.assertNull(oneyAddress.getLine3());
        Assertions.assertNull(oneyAddress.getLine4());
        Assertions.assertNull(oneyAddress.getLine5());


        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .fromPayline(getBuyer(street2, street1), Buyer.AddressType.BILLING)
                .build();

        Assertions.assertEquals("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", oneyAddress.getLine1());
        Assertions.assertEquals("BBBBBBB", oneyAddress.getLine2());
        Assertions.assertNull(oneyAddress.getLine3());
        Assertions.assertNull(oneyAddress.getLine4());
        Assertions.assertNull(oneyAddress.getLine5());


        street1 = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA       BBBBBBB";

        oneyAddress = OneyAddress.Builder.aOneyAddressBuilder()
                .fromPayline(getBuyer(street1, street2), Buyer.AddressType.BILLING)
                .build();

        Assertions.assertEquals("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", oneyAddress.getLine1());
        Assertions.assertEquals("BBBBBBB", oneyAddress.getLine2());
        Assertions.assertNull(oneyAddress.getLine3());
        Assertions.assertNull(oneyAddress.getLine4());
        Assertions.assertNull(oneyAddress.getLine5());

    }


    private Buyer getBuyer(String street1, String street2) throws ParseException {
        Buyer.Address address = Buyer.Address.AddressBuilder.anAddress()
                .withStreet1(street1)
                .withStreet2(street2)
                .withCity("Milano")
                .withZipCode("20100")
                .withCountry("IT")
                .withFullName(TestUtils.createFullName())
                .build();

        addresses.put(Buyer.AddressType.BILLING, address);

        return Buyer.BuyerBuilder.aBuyer()
                .withEmail("testoney." + RandomStringUtils.random(5, true, false) + "@gmail.com")
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .withFullName(TestUtils.createFullName())
                .withCustomerIdentifier("subscriber12")
                .withExtendedData(TestUtils.createDefaultExtendedData())
                .withBirthday(new SimpleDateFormat("dd/MM/yyyy").parse("04/05/1981"))
                .withLegalStatus(Buyer.LegalStatus.PERSON)
                .build();
    }

}
