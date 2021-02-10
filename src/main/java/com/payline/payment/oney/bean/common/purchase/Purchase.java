package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.Required;

import java.util.List;

public class Purchase extends OneyBean {

    @Required
    @SerializedName("external_reference_type")
    private String externalReferenceType; //CMDE

    @Required
    @SerializedName("external_reference")
    private String externalReference;

    @Required
    @SerializedName("purchase_amount")
    private Float purchaseAmount;

    @Required
    @SerializedName("currency_code")
    private String currencyCode; //ISO 4217

    @Required
    @SerializedName("delivery")
    private Delivery delivery;

    @Required
    @SerializedName("item_list")
    private List<Item> listItem;

    @SerializedName("number_of_items")
    private Integer numberOfItems;

    //getter

    public String getExternalReferenceType() {
        return externalReferenceType;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public Float getPurchaseAmount() {
        return purchaseAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }


    public Delivery getDelivery() {
        return delivery;
    }

    public Integer getNumberOfItems() {
        return numberOfItems;
    }

    public List<Item> getListItem() {
        return listItem;
    }


    //constructors
    private Purchase(Purchase.Builder builder) {
        this.externalReferenceType = builder.externalReferenceType;
        this.externalReference = builder.externalReference;
        this.purchaseAmount = builder.purchaseAmount;
        this.currencyCode = builder.currencyCode;
        this.delivery = builder.delivery;
        this.listItem = builder.listItem;
        this.numberOfItems = builder.numberOfItems;
    }

    private Purchase() {
    }


    //Builder
    public static class Builder {
        private String externalReferenceType; //CMDE
        private String externalReference;
        private Float purchaseAmount;
        private String currencyCode; //ISO 4217
        private Delivery delivery;
        private List<Item> listItem;
        private Integer numberOfItems;

        public static Purchase.Builder aPurchaseBuilder() {
            return new Purchase.Builder();
        }

        public Purchase.Builder withExternalReferenceType(String externalReferenceType) {
            this.externalReferenceType = externalReferenceType;
            return this;
        }

        public Purchase.Builder withExternalReference(String externalReference) {
            this.externalReference = externalReference;
            return this;
        }

        public Purchase.Builder withPurchaseAmount(Float purchaseAmount) {
            this.purchaseAmount = purchaseAmount;
            return this;
        }

        public Purchase.Builder withCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Purchase.Builder withDelivery(Delivery delivery) {
            this.delivery = delivery;
            return this;
        }

        public Purchase.Builder withListItem(List<Item> listItem) {
            this.listItem = listItem;
            return this;

        }

        public Purchase.Builder withNumberOfItems(Integer numberOfItems) {
            this.numberOfItems = numberOfItems;
            return this;
        }

        public Purchase build() {
            return new Purchase(this);
        }
    }
}
