package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.purchase.Item;
import com.payline.payment.oney.bean.common.purchase.Travel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static com.payline.payment.oney.utils.TestUtils.createAmount;
import static com.payline.payment.oney.utils.TestUtils.createOrderItem;

class ItemTest {

    private Item item;

    private static final Currency CURRENCY = Currency.getInstance("EUR");

    @Test
    void item() {
        item = Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label")
                .withPrice(175f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build();
        Assertions.assertNotNull(item.getIsMainItem());
        Assertions.assertNotNull(item.getItemExternalcode());
        Assertions.assertNotNull(item.getCategoryCode());
        Assertions.assertNotNull(item.getLabel());
        Assertions.assertNotNull(item.getPrice());
        Assertions.assertNotNull(item.getMarketplaceFlag());
        Assertions.assertNotNull(item.getQuantity());
    }

    @Test
    void itemFromPaylineRequest() {
        item = Item.Builder.aItemBuilder()
                .fromPayline(createOrderItem("someRef", createAmount(CURRENCY)))
                .build();

        Assertions.assertNotNull(item.getIsMainItem());
        Assertions.assertNotNull(item.getItemExternalcode());
        Assertions.assertNotNull(item.getCategoryCode());
        Assertions.assertNotNull(item.getLabel());
        Assertions.assertNotNull(item.getPrice());
        Assertions.assertNotNull(item.getQuantity());
    }

    @Test
    void testToString() {
        item = Item.Builder.aItemBuilder()
                .fromPayline(createOrderItem("someRefe", createAmount(CURRENCY)))
                .withTravel(new Travel.TravelBuilder().build())
                .build();

        Assertions.assertTrue(item.toString().contains("is_main_item"));
        Assertions.assertTrue(item.toString().contains("category_code"));
        Assertions.assertTrue(item.toString().contains("label"));
        Assertions.assertTrue(item.toString().contains("item_external_code"));
        Assertions.assertTrue(item.toString().contains("quantity"));
        Assertions.assertTrue(item.toString().contains("price"));


    }

    @Test
    void defineMainItem() {
        //to implement
        List<Item> itemList = new ArrayList<>();
        itemList.add(Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label1")
                .withPrice(10f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build());
        itemList.add(Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label2")
                .withPrice(25f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build());
        itemList.add(Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label3")
                .withPrice(15f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build());


        Item.defineMainItem(itemList);
        Assertions.assertEquals(0, (int) itemList.get(0).getIsMainItem());
        Assertions.assertEquals(1, (int) itemList.get(1).getIsMainItem());
        Assertions.assertEquals(0, (int) itemList.get(2).getIsMainItem());
    }

}
