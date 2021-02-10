package com.payline.payment.oney.bean.common.customer;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyAddress;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.Required;

public class Customer extends OneyBean {

    @SerializedName("trust_flag")
    private Integer trustFlag;

    @Required
    @SerializedName("customer_external_code")
    private String customerExternalCode;

    @Required
    @SerializedName("language_code")
    private String languageCode;

    @Required
    @SerializedName("identity")
    private CustomerIdentity identity;

    @Required
    @SerializedName("contact_details")
    private ContactDetails contactDetails;

    @Required
    @SerializedName("customer_address")
    private OneyAddress customerAddress;

    // a implementer plus tard  maybe
    @SerializedName("purchase_history")
    private PurchaseHistory purchaseHistory;

    @SerializedName("supporting_documents")
    private SupportingDocuments supportingDocuments;


    public Integer getTrustFlag() {
        return trustFlag;
    }

    public String getCustomerExternalCode() {
        return customerExternalCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public CustomerIdentity getIdentity() {
        return identity;
    }

    public ContactDetails getContactDetails() {
        return contactDetails;
    }

    public OneyAddress getCustomerAddress() {
        return customerAddress;
    }

    public PurchaseHistory getPurchaseHistory() {
        return purchaseHistory;
    }

    public SupportingDocuments getSupportingDocument() {
        return supportingDocuments;
    }

    private Customer() {
    }

    private Customer(Customer.Builder builder) {
        this.trustFlag = builder.trustFlag;
        this.customerExternalCode = builder.customerExternalCode;
        this.languageCode = builder.languageCode;
        this.identity = builder.identity;
        this.contactDetails = builder.contactDetails;
        this.customerAddress = builder.customerAddress;
        this.purchaseHistory = builder.purchaseHistory;
        this.supportingDocuments = builder.supportingDocuments;
    }

    public static class Builder {
        private Integer trustFlag;
        private String customerExternalCode;
        private String languageCode;
        private CustomerIdentity identity;
        private ContactDetails contactDetails;
        private OneyAddress customerAddress;
        private PurchaseHistory purchaseHistory;
        private SupportingDocuments supportingDocuments;


        public static Customer.Builder aCustomBuilder() {
            return new Customer.Builder();
        }

        public Customer.Builder withTrustFlag(Integer trustFlag) {
            this.trustFlag = trustFlag;
            return this;
        }

        public Customer.Builder withCustomerExternalCode(String code) {
            this.customerExternalCode = code;
            return this;
        }

        public Customer.Builder withLanguageCode(String code) {
            this.languageCode = code;
            return this;
        }

        public Customer.Builder withCustomerIdentity(CustomerIdentity id) {
            this.identity = id;
            return this;
        }

        public Customer.Builder withCustomerAddress(OneyAddress adr) {
            this.customerAddress = adr;
            return this;
        }

        public Customer.Builder withPurchaseHistory(PurchaseHistory pch) {
            this.purchaseHistory = pch;
            return this;
        }

        public Customer.Builder withContactDetails(ContactDetails details) {
            this.contactDetails = details;
            return this;
        }

        public Customer build() {
            return new Customer(this);
        }
    }
}
