package com.payline.payment.oney.bean.common.payment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.Required;

import java.util.List;


public class PaymentData extends OneyBean {

    @Required
    @Expose
    @SerializedName("payment_amount")
    private Float amount;

    @Required
    @SerializedName("currency_code")
    private String currency; // ISO 4217

    @SerializedName("payment_type")
    private Integer paymentType;

    @Required
    @SerializedName("business_transaction_list")
    private List<BusinessTransactionData> businessTransactionList;

    @Required
    @SerializedName("business_transaction")
    private BusinessTransactionData businessTransaction;


    public Float getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public List<BusinessTransactionData> getBusinessTransactionList() {
        return businessTransactionList;
    }

    public BusinessTransactionData getBusinessTransaction() {
        return businessTransaction;
    }

    private PaymentData() {
    }

    private PaymentData(PaymentData.Builder builder) {
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.paymentType = builder.paymentType;
        this.businessTransactionList = builder.businessTransactionList;
        this.businessTransaction = builder.businessTransaction;
    }

    public static class Builder {
        private Float amount;
        private String currency;
        private Integer paymentType;
        private List<BusinessTransactionData> businessTransactionList;
        private BusinessTransactionData businessTransaction;

        public static PaymentData.Builder aPaymentData() {
            return new PaymentData.Builder();
        }

        public PaymentData.Builder withAmount(float amount) {
            this.amount = amount;
            return this;
        }

        public PaymentData.Builder withCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentData.Builder withPaymentType(int paymentType) {
            this.paymentType = paymentType;
            return this;
        }

        public PaymentData.Builder withPaymentType(List<BusinessTransactionData> businessTransactionList) {
            this.businessTransactionList = businessTransactionList;
            return this;
        }

        public PaymentData.Builder withBusinessTransactionList(BusinessTransactionData businessTransaction) {
            this.businessTransaction = businessTransaction;
            return this;
        }

        public PaymentData build() {
            return new PaymentData(this);
        }

        public PaymentData buildForConfirmRequest() {
            return new PaymentData(this);
        }

    }


}
