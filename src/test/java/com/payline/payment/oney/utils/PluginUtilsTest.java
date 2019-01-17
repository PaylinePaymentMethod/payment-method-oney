package com.payline.payment.oney.utils;

import com.payline.payment.oney.bean.common.purchase.Item;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Currency;
import java.util.Map;

import static com.payline.payment.oney.bean.common.enums.CategoryCodeHandler.findCategory;
import static com.payline.payment.oney.utils.PluginUtils.*;

public class PluginUtilsTest {


    @Test
    public void testTruncateText() {
        String longText = "Update: yes it doesn't take the slim characters into account but I don't agree with that considering everyone has different screens and fonts setup and a large portion of the people that land here on this page are probably looking for a maintained library like the above.";
        String longText2 = "Update:";
        Map textCutted = truncateLongText(longText, longText2, 19);
        Assertions.assertTrue(textCutted.get("line1").toString().length() < 20);
        Assertions.assertTrue(textCutted.get("line2") == null || textCutted.get("line2").toString().length() < 20);
        Assertions.assertTrue(textCutted.get("line3") == null || textCutted.get("line3").toString().length() < 20);
        Assertions.assertTrue(textCutted.get("line4") == null || textCutted.get("line4").toString().length() < 20);
        Assertions.assertTrue(textCutted.get("line5") == null || textCutted.get("line5").toString().length() < 20);

    }

    @Test
    public void itemComparator() {
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
        String mr = "4";
        String mme = "1";
        String miss = "3";
        int hCodeOney = getHonorificCode(mr);
        int hCodeOney2 = getHonorificCode(mme);
        int hCodeOney3 = getHonorificCode(miss);

        Assertions.assertEquals(1, hCodeOney);
        Assertions.assertEquals(2, hCodeOney2);
        Assertions.assertEquals(3, hCodeOney3);

    }

    @Test
    public void createStringAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals("0.00", PluginUtils.createStringAmount(int1,Currency.getInstance("EUR")));
        Assertions.assertEquals("0.01", PluginUtils.createStringAmount(int2,Currency.getInstance("EUR")));
        Assertions.assertEquals("0.10", PluginUtils.createStringAmount(int3,Currency.getInstance("EUR")));
        Assertions.assertEquals("1.00", PluginUtils.createStringAmount(int4,Currency.getInstance("EUR")));
        Assertions.assertEquals("10.00", PluginUtils.createStringAmount(int5,Currency.getInstance("EUR")));
    }

    @Test
    public void createFloatAmount() {
        BigInteger int1 = BigInteger.ZERO;
        BigInteger int2 = BigInteger.ONE;
        BigInteger int3 = BigInteger.TEN;
        BigInteger int4 = BigInteger.valueOf(100);
        BigInteger int5 = BigInteger.valueOf(1000);

        Assertions.assertEquals(new Float("00.00"), PluginUtils.createFloatAmount(int1, Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("00.01"), PluginUtils.createFloatAmount(int2,Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("00.10"), PluginUtils.createFloatAmount(int3,Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("1.00"), PluginUtils.createFloatAmount(int4,Currency.getInstance("EUR")));
        Assertions.assertEquals(new Float("10.00"), PluginUtils.createFloatAmount(int5,Currency.getInstance("EUR")));
    }

    @Test
    public void getRefundFlagTrue() {
        String status = "FUNDED";
        boolean flag = getRefundFlag(status);
        Assertions.assertTrue(flag);

    }

    @Test
    public void getRefundFlagFalse() {
        String status = "FAVORABLE";
        String status2 = "PENDING";
        boolean flag = getRefundFlag(status);
        boolean flag2 = getRefundFlag(status);

        Assertions.assertFalse(flag);
        Assertions.assertFalse(flag2);

    }

    @Test
    public void getRefundFlagInvalid() {

        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {

            String status = "XOXO";
            boolean flag = getRefundFlag(status);
        });
        Assertions.assertEquals("XOXO is not a valid status for refund or cancel", exception.getMessage());
    }

    @Test
    public void getRefundFlagNotRefundable() {
        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {

            String status = "REFUSED";
            boolean flag = getRefundFlag(status);

        });

        Assertions.assertEquals("a REFUSED transactionStatusRequest can't be cancelled", exception.getMessage());

    }
}
