package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

public class Item extends OneyBean {

    @SerializedName("is_main_item")
    private Integer isMainItem; // faire booleen  ??
    //fixme mapping payline oney compliqué, pas assez detaillé coté Payline
    @SerializedName("category_code")
    private Integer categoryCode; // faire Oney value - Payline  ??
    private String label; // faire Oney value - Payline  ??
    @SerializedName("item_external_code")
    private String itemExternalcode; // faire Oney value - Payline  ??
    private Integer quantity;
    private Float price;
    @SerializedName("marketplace_merchant_flag")
    private Integer marketplaceFlag; // faire booleen  ??
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

    //Builder
    public static class Builder {
        private Integer isMainItem; // faire booleen  ??
        private Integer categoryCode; // faire Oney value - Payline  ??
        private String label; // faire Oney value - Payline  ??
        private String itemExternalcode; // faire Oney value - Payline  ??
        private Integer quantity;
        private Float price;
        private Integer marketplaceFlag; // faire booleen  ??
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
            this.categoryCode = 0;
            this.label = item.getComment(); //or get Brand +" "+ get comment ?
            this.itemExternalcode = item.getReference();
            this.quantity = item.getQuantity().intValue();
            this.price = item.getAmount().getAmountInSmallestUnit().floatValue();
            //todo mapper selon marketplace
            this.marketplaceFlag = 0;
            this.marketplaceName = null;
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
            if (this.marketplaceFlag == 1 && this.marketplaceName == null) {
                throw new IllegalStateException("Item must have a marketplaceName when built");
            }
            return this;
        }

        public Item build() {
            return new Item(this.verifyIntegrity());
        }
    }

}
