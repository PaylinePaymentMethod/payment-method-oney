package com.payline.payment.oney.utils;

import com.payline.payment.oney.bean.common.purchase.Item;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.powermock.reflect.Whitebox;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.payline.payment.oney.bean.common.enums.CategoryCodeHandler.findCategory;
import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.PluginUtils.*;
import static com.payline.payment.oney.utils.TestUtils.createDefaultPaymentRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PluginUtilsTest {

    private static final Logger LOGGER = LogManager.getLogger(PluginUtilsTest.class);

    String merchantId1;

    private PaymentRequest paymentRequest;

    private PartnerConfiguration partnerConfiguration;

    private ContractConfiguration contractConfiguration;

    @BeforeAll
    public void setUp() {
        paymentRequest = TestUtils.createCompletePaymentRequestBuilder().build();
        partnerConfiguration = paymentRequest.getPartnerConfiguration();
        contractConfiguration = paymentRequest.getContractConfiguration();
        Whitebox.setInternalState(partnerConfiguration, "partnerConfigurationMap", new HashMap<>());
        Whitebox.setInternalState(partnerConfiguration, "sensitivePartnerConfigurationMap", new HashMap<>());
        Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());


    }

    @Test
    public void spaceConcat_nominal() {
        // given 2 strings
        String text1 = "This is the first part";
        String text2 = "This is the second part";

        // when: concatenating
        String result = PluginUtils.spaceConcat(text1, text2);

        // expected
        assertEquals(text1 + " " + text2, result);
    }

    @Test
    public void spaceConcat_trailingSpace() {
        // given 2 strings
        String text1 = "The first part with a space at the end ";
        String text2 = "the second part";

        // when: concatenating
        String result = PluginUtils.spaceConcat(text1, text2);

        // expected
        assertEquals(text1 + text2, result);
    }

    @Test
    public void splitLongText_nothingToSplit() {
        // given:
        String toSplit = "This string's length is just the maxLength";

        // when: splitting
        List<String> result = PluginUtils.splitLongText(toSplit, toSplit.length());

        // then: there is only one line, equal to the original string
        assertEquals(1, result.size());
        assertEquals(toSplit, result.get(0));
    }

    @Test
    public void splitLongText_splitAfterSpace() {
        // given:
        String toSplit = "This string will be split in two";

        // when: splitting
        List<String> result = PluginUtils.splitLongText(toSplit, 17);

        // then: two lines, the first does not end with a space
        assertEquals(2, result.size());
        assertFalse(result.get(0).endsWith(" "));
    }

    @Test
    public void splitLongText_splitBeforeSpace() {
        // given:
        String toSplit = "This string will be split in two";

        // when: splitting
        List<String> result = PluginUtils.splitLongText(toSplit, 16);

        // then: two lines, the second does not start with a space
        assertEquals(2, result.size());
        assertFalse(result.get(1).startsWith(" "));
    }

    @Test
    public void splitLongText_splitMiddleWord() {
        // given:
        String toSplit = "This string should not be split in the middle of a word";

        // when: splitting
        List<String> result = PluginUtils.splitLongText(toSplit, 28);

        // then: expect 3 lines "This string should not be", "split in the middle of a" and "word"
        assertEquals(3, result.size());
        assertEquals("This string should not be", result.get(0));
        assertEquals("split in the middle of a", result.get(1));
        assertEquals("word", result.get(2));
    }

    @Test
    public void splitLongText_multipleSplitSpaces() {
        // given:
        String toSplit = "This string has a very      long space in the middle";

        // when: splitting
        List<String> result = PluginUtils.splitLongText(toSplit, 25);

        // then: expect 2 lines, none with a trailing space
        assertEquals(2, result.size());
        assertEquals("This string has a very", result.get(0));
        assertEquals("long space in the middle", result.get(1));
    }

    @Test
    public void splitLongText_tooLongWithoutSpace() {
        // given:
        String toSplit = "ThisStringIsTooLongButThereIsNoSpaceBetweenWordsToSplitCleanly";

        // when: splitting
        List<String> result = PluginUtils.splitLongText(toSplit, 20);

        // then: expect 4 lines
        assertEquals(4, result.size());
    }

    @Test
    public void itemComparator() throws Exception {
        ItemComparator comp = new ItemComparator();
        Item item1 = Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label")
                .withPrice(110f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build();
        Item item2 = Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label")
                .withPrice(40f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build();

        int highest = comp.compare(item1, item2);
        Assertions.assertEquals(1, highest);
    }

    @Test
    public void testGetIsoAlpha3CodeFromCountryCode2() {
        String country = "FR";
        String pays = getIsoAlpha3CodeFromCountryCode2(country);
        Assertions.assertEquals("FRA", pays);

    }

    @Test
    public void testGetCountryNameCodeFromCountryCode2() {
        String country = "FR";
        String pays = getCountryNameCodeFromCountryCode2(country);
        Assertions.assertEquals("France", pays);


    }


    @Test
    public void testCategoryHandler() {
        String catCodePayline = "17";
        String catCodePayline2 = "964";
        int catCodeOney = findCategory(catCodePayline);
        int catCodeOney2 = findCategory(catCodePayline2);

        Assertions.assertEquals(1, catCodeOney);
        Assertions.assertEquals(5, catCodeOney2);

    }

    @Test
    public void testDeliveryModeCode() {
        String catCodePayline = "1";
        String catCodePayline2 = "4";
        int catCodeOney = getOneyDeliveryModeCode(catCodePayline);
        int catCodeOney2 = getOneyDeliveryModeCode(catCodePayline2);

        Assertions.assertEquals(1, catCodeOney);
        Assertions.assertEquals(4, catCodeOney2);
    }

    @Test
    public void testDeliveryOption() {
        String mode1 = "1";
        String mode2 = "2";
        String mode3 = "17";
        int catCodeOney = getOneyDeliveryOption(mode1);
        int catCodeOney2 = getOneyDeliveryOption(mode2);
        int catCodeOney3 = getOneyDeliveryOption(mode3);

        Assertions.assertEquals(1, catCodeOney);
        Assertions.assertEquals(2, catCodeOney2);
        Assertions.assertEquals(2, catCodeOney3);
    }

    @Test
    public void testHonorificName() {

        // Madame → PAYLINE: 1 → ONEY: 2
        String mme = "1";
        int hCodeOney2 = getHonorificCode(mme);
        Assertions.assertEquals(2, hCodeOney2);

        // Mademoiselle → PAYLINE: 3 → ONEY: 3
        String miss = "3";
        int hCodeOney3 = getHonorificCode(miss);
        Assertions.assertEquals(3, hCodeOney3);

        // Monsieur → PAYLINE: 4 → ONEY: 1
        String mr = "4";
        int hCodeOney1 = getHonorificCode(mr);
        Assertions.assertEquals(1, hCodeOney1);

        // Toutes les autres valeurs PAYLINE → ONEY 0
        String others;
        for (int i = 0; i < 11; i++) {
            if (i == 1 || i == 3 || i == 4) {
                continue;
            }
            others = ((Integer) i).toString();
            int hCodeOney0 = getHonorificCode(others);
            Assertions.assertEquals(0, hCodeOney0);
        }

    }

    @Test
    public void createStringAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals("0.00", PluginUtils.createStringAmount(int1, Currency.getInstance("EUR")));
        Assertions.assertEquals("0.01", PluginUtils.createStringAmount(int2, Currency.getInstance("EUR")));
        Assertions.assertEquals("0.10", PluginUtils.createStringAmount(int3, Currency.getInstance("EUR")));
        Assertions.assertEquals("1.00", PluginUtils.createStringAmount(int4, Currency.getInstance("EUR")));
        Assertions.assertEquals("10.00", PluginUtils.createStringAmount(int5, Currency.getInstance("EUR")));
    }

    @Test
    public void createFloatAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals(new Float("00.00"), PluginUtils.createFloatAmount(int1, Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("00.01"), PluginUtils.createFloatAmount(int2, Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("00.10"), PluginUtils.createFloatAmount(int3, Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("1.00"), PluginUtils.createFloatAmount(int4, Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("10.00"), PluginUtils.createFloatAmount(int5, Currency.getInstance("EUR")));
    }

    @Test
    public void testIsISO639() {
        Assertions.assertFalse(isISO639("FR"));
        Assertions.assertTrue(isISO639("fr"));
    }

    @Test
    public void testIsISO3166() {
        Assertions.assertFalse(isISO3166("FRA"));
        Assertions.assertTrue(isISO3166("FR"));
    }

    @Test
    public void getParameters_noCoutryCode() {
        Throwable exception = Assertions.assertThrows(InvalidDataException.class, () -> {

            Whitebox.setInternalState(partnerConfiguration, "partnerConfigurationMap", new HashMap<>());
            Whitebox.setInternalState(partnerConfiguration, "sensitivePartnerConfigurationMap", new HashMap<>());
            Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());
            getParametersMap(paymentRequest);

        });

    }

    @Test
    public void getParameters_emptyCoutryCode() {
        Throwable exception = Assertions.assertThrows(InvalidDataException.class, () -> {

            Whitebox.setInternalState(partnerConfiguration, "partnerConfigurationMap", new HashMap<>());
            Whitebox.setInternalState(partnerConfiguration, "sensitivePartnerConfigurationMap", new HashMap<>());
            Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());

            Map<String, String> contractConfigurationMap = new HashMap<>();
            contractConfigurationMap.put(COUNTRY_CODE_KEY, "");
            Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());
            getParametersMap(paymentRequest);

        });

    }

    @Test
    public void getParameters_noAuthorizationKey() {
        Throwable exception = Assertions.assertThrows(InvalidDataException.class, () -> {


            Map<String, String> partnerConfigurationMap = new HashMap<>();
            partnerConfigurationMap.put(PARTNER_API_URL, "PARTNER_API_URL");
            Whitebox.setInternalState(partnerConfiguration, "partnerConfigurationMap", partnerConfigurationMap);
            Whitebox.setInternalState(partnerConfiguration, "sensitivePartnerConfigurationMap", new HashMap<>());
            Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());
            Map<String, String> contractConfigurationMap = new HashMap<>();
            contractConfigurationMap.put(COUNTRY_CODE_KEY, "coutryCode");
            Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());
            getParametersMap(paymentRequest);

        });

    }

    @Test
    public void getParameters_noPartnerUrl() {
        Throwable exception = Assertions.assertThrows(InvalidDataException.class, () -> {
            Map<String, String> partnerConfigurationMap = new HashMap<>();
            partnerConfigurationMap.put(PARTNER_API_URL, "PARTNER_AUTHORIZATION_KEY");
            Whitebox.setInternalState(partnerConfiguration, "partnerConfigurationMap", partnerConfigurationMap);
            Whitebox.setInternalState(partnerConfiguration, "sensitivePartnerConfigurationMap", new HashMap<>());
            Map<String, String> contractConfigurationMap = new HashMap<>();
            contractConfigurationMap.put(COUNTRY_CODE_KEY, "coutryCode");
            Whitebox.setInternalState(contractConfiguration, "contractProperties", new HashMap<>());
            getParametersMap(paymentRequest);

        });

    }

    @Test
    public void getParameters_ok() throws Exception {
        Map<String, String> partnerConfigurationMap = new HashMap<>();
        partnerConfigurationMap.put(PARTNER_AUTHORIZATION_KEY + ".coutrycode", "PARTNER_AUTHORIZATION_KEY");
        partnerConfigurationMap.put(PARTNER_API_URL, "PARTNER_API_URL");
        Whitebox.setInternalState(partnerConfiguration, "partnerConfigurationMap", partnerConfigurationMap);
        Whitebox.setInternalState(partnerConfiguration, "sensitivePartnerConfigurationMap", new HashMap<>());
        Map<String, ContractProperty> contractConfigurationMap = new HashMap<>();
        contractConfigurationMap.put(COUNTRY_CODE_KEY, new ContractProperty("coutryCode"));
        Whitebox.setInternalState(contractConfiguration, "contractProperties", contractConfigurationMap);

        Map<String, String> result = getParametersMap(paymentRequest);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.containsKey(PARTNER_AUTHORIZATION_KEY));
        Assertions.assertTrue(result.containsKey(PARTNER_API_URL));
        Assertions.assertTrue(result.containsKey(HEADER_COUNTRY_CODE));
        Assertions.assertTrue(result.containsKey(SECRET_KEY));
        Assertions.assertEquals(4, result.size());

    }

    @Test
    public void testGenerateMerchantRequestId() throws Exception {

        merchantId1 = generateMerchantRequestId("merchantId");
        TimeUnit.SECONDS.sleep(1);
        String merchantId2 = generateMerchantRequestId("merchantId");
        Assertions.assertNotEquals(merchantId1, merchantId2);
    }


    @Test
    public void truncate() {
        Assertions.assertEquals("0123456789", PluginUtils.truncate("01234567890123456789", 10));
        Assertions.assertEquals("01234567890123456789", PluginUtils.truncate("01234567890123456789", 60));
        Assertions.assertEquals("", PluginUtils.truncate("", 30));
        Assertions.assertNull(PluginUtils.truncate(null, 30));
    }

    @Test
    void createMerchantContext() {
        String expectedMerchantContext = "CL!40800!EUR";
        PaymentRequest request = createDefaultPaymentRequest();

        //  creation test
        String merchantContext = PluginUtils.createMerchantContext(request);
        Assertions.assertEquals(expectedMerchantContext, merchantContext);

        // recorery test
        Assertions.assertEquals(false, PluginUtils.isCaptureNow(merchantContext));
        Assertions.assertEquals(408.00f, PluginUtils.getAmount(merchantContext));
        Assertions.assertEquals("EUR", PluginUtils.getCurrency(merchantContext));
    }


    @Test
    void readMerchantContextCN() {
        String merchantContext = "CN!17204!EUR";

        // recorery test
        Assertions.assertEquals(true, PluginUtils.isCaptureNow(merchantContext));
        float toto =  PluginUtils.getAmount(merchantContext);
        Assertions.assertEquals(172.04f,toto);
        Assertions.assertEquals("EUR", PluginUtils.getCurrency(merchantContext));
    }

    @Test
    void dateToString() throws InvalidDataException {
        Date date = new Date();
        String sDate = PluginUtils.dateToString(date);

        Assertions.assertNotNull(sDate);
        Assertions.assertTrue(sDate.matches("[0-9]{4}-[0-1][0-9]-[0-3][0-9]"));
    }

    @Test
    void dateToStringException () {
        Throwable exception = Assertions.assertThrows(InvalidDataException.class, () -> {
            Date date = null;
            String sDate = PluginUtils.dateToString(date);
        });


    }

}
