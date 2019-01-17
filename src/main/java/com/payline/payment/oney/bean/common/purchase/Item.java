package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.ItemComparator;
import com.payline.pmapi.bean.payment.Order;

import java.util.Collections;
import java.util.List;

import static com.payline.payment.oney.bean.common.enums.CategoryCodeHandler.findCategory;
import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;

public class Item extends OneyBean {

    @SerializedName("is_main_item")
    private Integer isMainItem;
    @SerializedName("category_code")
    private Integer categoryCode;
    private String label;
    @SerializedName("item_external_code")
    private String itemExternalcode;
    private Integer quantity;
    private Float price;
    @SerializedName("marketplace_merchant_flag")
    private Integer marketplaceFlag;
    @SerializedName("marketplace_merchant_name")
    private String marketplaceName;
    @SerializedName("travel")
    private Travel travel;

    //Getter

    public Integer getIsMainItem() {
        return isMainItem;
    }

    public Integer getCategoryCode() {
        return categoryCode;
    }

    public String getLabel() {
        return label;
    }

    public String getItemExternalcode() {
        return itemExternalcode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Float getPrice() {
        return price;
    }

    public Integer getMarketplaceFlag() {
        return marketplaceFlag;
    }

    public String getMarketplaceName() {
        return marketplaceName;
    }

    public Travel getTravel() {
        return travel;
    }

    //Setter
    public void setIsMainItem(Integer isMainItem) {
        this.isMainItem = isMainItem;
    }
    //constructors

    private Item() {
    }


    public Item(Item.Builder builder) {
        this.categoryCode = builder.categoryCode;
        this.label = builder.label;
        this.itemExternalcode = builder.itemExternalcode;
        this.quantity = builder.quantity;
        this.price = builder.price;
        this.marketplaceFlag = builder.marketplaceFlag;
        this.marketplaceName = builder.marketplaceName;
        this.travel = builder.travel;
        this.isMainItem = builder.isMainItem;
    }

    /**
     * Compare the price of each item and set main item to 1
     * for the most expensive item
     *
     * @param listItems a list of Item
     */
    public static void defineMainItem(List<Item> listItems) {
        if (listItems.isEmpty()) {
            throw new IllegalArgumentException("This list not contain any item");
        }
        if (listItems.size() == 1) {
            listItems.get(0).setIsMainItem(1);
        } else {
            Item maxItem = Collections.max(listItems, new ItemComparator());
            listItems.forEach(item -> {
                if (item.equals(maxItem)) {
                    item.setIsMainItem(1);
                }
            });
        }

    }

    //Builder
    public static class Builder {
        private Integer isMainItem;
        private Integer categoryCode;
        private String label;
        private String itemExternalcode;
        private Integer quantity;
        private Float price;
        private Integer marketplaceFlag;
        private String marketplaceName;
        private Travel travel;

        public static Item.Builder aItemBuilder() {
            return new Item.Builder();
        }

        public Item.Builder withItemExternalCode(String itemExternalcode) {
            this.itemExternalcode = itemExternalcode;
            return this;
        }

        public Item.Builder withMainItem(Integer isMainItem) {
            this.isMainItem = isMainItem;
            return this;
        }

        public Item.Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Item.Builder withQuantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Item.Builder withPrice(Float price) {
            this.price = price;
            return this;
        }

        public Item.Builder withCategoryCode(Integer category) {
            this.categoryCode = category;
            return this;
        }

        public Item.Builder withMarketplaceName(String name) {
            this.marketplaceName = name;
            return this;
        }

        public Item.Builder withMarketplaceFlag(Integer marketplaceFlag) {
            this.marketplaceFlag = marketplaceFlag;
            return this;
        }

        public Item.Builder withTravel(Travel travel) {
            this.travel = travel;
            return this;
        }

        public Item.Builder fromPayline(Order.OrderItem item) {
            this.isMainItem = 0;
            this.categoryCode = findCategory(item.getCategory());
            this.label = item.getComment(); //or get Brand +" "+ get comment ?
            this.itemExternalcode = item.getReference();
            this.quantity = item.getQuantity().intValue();
            this.price = createFloatAmount(item.getAmount().getAmountInSmallestUnit(),item.getAmount().getCurrency());
            //note HME  mapper selon marketplace lot 2
//            this.marketplaceFlag = 0;
//            this.marketplaceName = null;
            this.travel = null;

            return this;
        }

        private Item.Builder verifyIntegrity() {
            if (this.label == null) {
                throw new IllegalStateException("Item must have a label when built");
            }
            if (this.isMainItem == null) {
                throw new IllegalStateException("Item must have a isMainItem when built");
            }
            if (this.categoryCode == null) {
                throw new IllegalStateException("Item must have a categoryCode when built");
            }
            if (this.itemExternalcode == null) {
                throw new IllegalStateException("Item must have a itemExternalcode when built");
            }
            if (this.quantity == null) {
                throw new IllegalStateException("Item must have a quantity when built");
            }
            if (this.price == null) {
                throw new IllegalStateException("Item must have a price when built");
            }
            if (this.marketplaceFlag != null && this.marketplaceFlag == 1 && this.marketplaceName == null) {
                throw new IllegalStateException("Item must have a marketplaceName when built");

            }
            return this;
        }

        public Item build() {
            return new Item(this.verifyIntegrity());
        }
    }

}
