package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.utils.Required;

public class LoyaltyInformation extends OneyBean {

    @Required
    @SerializedName("loyalty_id")
    private String loyaltyId;

    @SerializedName("loyalty_faq_url")
    private String loyaltyFaqUrl;

    @SerializedName("expiration_date")
    private String expirationDate; //date yyyy-MM-dd

    @SerializedName("representation")
    private String representation;

    @SerializedName("type")
    private String type;

    @SerializedName("value")
    private String value;

    // Getter
    public String getLoyaltyId() {
        return loyaltyId;
    }

    public String getLoyaltyFaqUrl() {
        return loyaltyFaqUrl;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getRepresentation() {
        return representation;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public LoyaltyInformation(LoyaltyInformation.Builder builder) {
        this.loyaltyId = builder.loyaltyId;
        this.loyaltyFaqUrl = builder.loyaltyFaqUrl;
        this.expirationDate = builder.expirationDate;
        this.representation = builder.representation;
        this.type = builder.type;
        this.value = builder.value;
    }

    //Constructor
    private LoyaltyInformation() {
    }

    // Builder

    public static class Builder {
        private String loyaltyId;
        private String loyaltyFaqUrl;
        private String expirationDate; //date yyyy-MM-dd
        private String representation;
        private String type;
        private String value;


        public static LoyaltyInformation.Builder aLoyaltyInformationBuilder() {
            return new LoyaltyInformation.Builder();
        }

        public LoyaltyInformation.Builder withLoyaltyId(String id) {
            this.loyaltyId = id;
            return this;
        }

        public LoyaltyInformation.Builder withLoyatyFaqUrl(String url) {
            this.loyaltyFaqUrl = url;
            return this;
        }

        public LoyaltyInformation.Builder withExpirationDate(String date) {
            this.expirationDate = date;
            return this;
        }

        public LoyaltyInformation.Builder withRepresentation(String representation) {
            this.representation = representation;
            return this;
        }

        public LoyaltyInformation.Builder withType(String type) {
            this.type = type;
            return this;
        }

        public LoyaltyInformation.Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public LoyaltyInformation build() {
            return new LoyaltyInformation(this);
        }
    }
}
