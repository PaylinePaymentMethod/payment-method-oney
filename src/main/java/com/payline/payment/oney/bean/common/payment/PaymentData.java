package com.payline.payment.oney.bean.common.payment;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.bean.common.enums.PaymentType;


public class PaymentData extends OneyBean {

    @Expose
    @SerializedName("payment_amount")
    private Float amount;
    @SerializedName("currency_code")
    private String currency; // ISO 4217
    @SerializedName("payment_type")
    private Integer paymentType;
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

    public BusinessTransactionData getBusinessTransaction() {
        return businessTransaction;
    }

    private PaymentData() {
    }

    private PaymentData(PaymentData.Builder builder) {
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.paymentType = builder.paymentType;
        this.businessTransaction = builder.businessTransaction;
    }

    public static class Builder {
        private Float amount;
        private String currency;
        private Integer paymentType;
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

        public PaymentData.Builder withBusinessTransactionList(BusinessTransactionData businessTransaction) {
            this.businessTransaction = businessTransaction;
            return this;
        }


        private PaymentData.Builder verifyIntegrity() {
            if (this.amount == null) {
                throw new IllegalStateException("PaymentData must have a amount when built");
            }
            if (this.currency == null) {
                throw new IllegalStateException("PaymentData must have a currency when built");
            }

            if (this.businessTransaction == null && (this.paymentType == null || this.paymentType != PaymentType.CHECK_CARD.getValue())) {
                throw new IllegalStateException("PaymentData must have a businessTransaction when built");

            } else {
                return this;
            }
        }

        public PaymentData build() {
            return new PaymentData(this.verifyIntegrity());
        }

        public PaymentData buildForConfirmRequest() {
            if (this.amount == null) {
                throw new IllegalStateException("PaymentData must have a amount when built");
            }
            return new PaymentData(this);
        }

    }


}
