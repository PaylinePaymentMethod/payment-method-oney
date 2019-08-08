package com.payline.payment.oney.bean.common.customer;

import com.google.gson.annotations.SerializedName;

/**
 * Pour v2
 */
@SuppressWarnings(value = "emptyClass")
public class PurchaseHistory {

    @SerializedName("total_number_of_purchase")
    private Integer numberOfPurchase;

    @SerializedName("total_amount")
    private Float totalAmount;

    @SerializedName("first_purchase_date")
    private String firstPurchasedate;

    @SerializedName("last_purchase_date")
    private String lastPurchaseDate;

    public Integer getNumberOfPurchase() {
        return numberOfPurchase;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public String getFirstPurchasedate() {
        return firstPurchasedate;
    }

    public String getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    private PurchaseHistory(Builder builder) {
        this.numberOfPurchase = builder.numberOfPurchase;
        this.totalAmount = builder.totalAmount;
        this.firstPurchasedate = builder.firstPurchasedate;
        this.lastPurchaseDate = builder.lastPurchaseDate;
    }

    // Builder
    public static class Builder {
        private int numberOfPurchase;
        private float totalAmount;
        private String firstPurchasedate;
        private String lastPurchaseDate;

        public static PurchaseHistory.Builder aPurchaseHistoryBuilder() { return new Builder();}

        public PurchaseHistory.Builder withNumberOfPurchase(int numberOfPurchase) {
            this.numberOfPurchase = numberOfPurchase;
            return this;
        }

        public PurchaseHistory.Builder withTotalAmount(float totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public PurchaseHistory.Builder withFirstPurchasedate(String firstPurchasedate) {
            this.firstPurchasedate = firstPurchasedate;
            return this;
        }

        public PurchaseHistory.Builder withLastPurchaseDate(String lastPurchaseDate) {
            this.lastPurchaseDate = lastPurchaseDate;
            return this;
        }

        public PurchaseHistory build() {
            return new PurchaseHistory(this);
        }


    }
}
