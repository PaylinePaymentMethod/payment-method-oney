package com.payline.payment.oney.bean.common.customer;

import com.google.gson.annotations.SerializedName;

/**
 * Purchase history data
 */
public class PurchaseHistory {

    @SerializedName("total_number_of_purchase")
    private Integer totalNumberOfPurchase;

    @SerializedName("total_amount")
    private Float totalAmount;

    @SerializedName("first_purchase_date")
    private String firstPurchaseDate;

    @SerializedName("last_purchase_date")
    private String lastPurchaseDate;

    public Integer getTotalNumberOfPurchase() {
        return totalNumberOfPurchase;
    }

    public Float getTotalAmount() {
        return totalAmount;
    }

    public String getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public String getLastPurchaseDate() {
        return lastPurchaseDate;
    }

    private PurchaseHistory(Builder builder) {
        this.totalNumberOfPurchase = builder.totalNumberOfPurchase;
        this.totalAmount = builder.totalAmount;
        this.firstPurchaseDate = builder.firstPurchaseDate;
        this.lastPurchaseDate = builder.lastPurchaseDate;
    }

    // Builder
    public static class Builder {
        private Integer totalNumberOfPurchase;
        private Float totalAmount;
        private String firstPurchaseDate;
        private String lastPurchaseDate;

        public static PurchaseHistory.Builder aPurchaseHistoryBuilder() { return new Builder();}

        public PurchaseHistory.Builder withTotalNumberOfPurchase(int totalNumberOfPurchase) {
            this.totalNumberOfPurchase = totalNumberOfPurchase;
            return this;
        }

        public PurchaseHistory.Builder withTotalAmount(float totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public PurchaseHistory.Builder withFirstPurchaseDate(String firstPurchaseDate) {
            this.firstPurchaseDate = firstPurchaseDate;
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
