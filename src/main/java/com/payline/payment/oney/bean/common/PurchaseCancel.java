package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;

public class PurchaseCancel extends OneyBean{
    @SerializedName("cancellation_reason_code")
    private Integer reasonCode; //or enum (0 = cancellation, 1 = fraud)
    @SerializedName("cancellation_amount")
    private Float amount; //must be present  if cancellationReason == 1  !! default value == total amount of payment
    @SerializedName("refund_down_payment")
    private boolean refundFlag; // must be present  if cancellationReason == 1 true if is refunded

    public int getReasonCode() {
        return reasonCode;
    }

    public float getAmount() {
        return amount;
    }

    public boolean isRefundFlag() {
        return refundFlag;
    }

    private PurchaseCancel(){}

    private PurchaseCancel(PurchaseCancel.Builder builder) {
        this.reasonCode = builder.reasonCode;
        this.amount = builder.amount;
        this.refundFlag = builder.refundFlag;
    }

    public static class Builder {
        private Integer reasonCode;
        private Float amount;
        private Boolean refundFlag;

        public static PurchaseCancel.Builder aPurchaseCancelBuilder() {
            return new PurchaseCancel.Builder();
        }

        public PurchaseCancel.Builder withReasonCode(int reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        public PurchaseCancel.Builder withAmount(float amount) {
            this.amount = amount;
            return this;
        }
        public PurchaseCancel.Builder withRefundFlag(boolean flag) {
            this.refundFlag = flag;
            return this;
        }

        public PurchaseCancel.Builder verifyIntegrity(){
            if (this.reasonCode == null || this.reasonCode > 1 ) {
                throw new IllegalStateException("PurchaseCancel must have a valid reasonCode when built");
            }
            if (this.reasonCode == 0 && this.amount == null) {
                throw new IllegalStateException("PurchaseCancel must have a amount when built");
            }
            if (this.reasonCode == 1 && this.refundFlag == null) {
                throw new IllegalStateException("PurchaseCancel must have a refundFlag when built");
            }

            else return this;

        }


        public PurchaseCancel build(){
            return new PurchaseCancel(this.verifyIntegrity());
        }
    }
}