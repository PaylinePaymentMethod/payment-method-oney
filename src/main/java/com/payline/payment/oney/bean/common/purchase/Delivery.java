package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyAddress;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

public class Delivery extends OneyBean {

    @SerializedName("delivery_date")
    private String deliveryDate;
    @SerializedName("delivery_mode_code")
    private Integer deliveryModeCode; //creer enum ?
    @SerializedName("delivery_option")
    private Integer deliveryOption;
    @SerializedName("priority_delivery_code")
    private Integer priorityDeliveryCode;
    @SerializedName("address_type")
    private Integer addressType; //Creer ENUM ??
    private Recipient recipient;
    @SerializedName("delivery_address")
    private OneyAddress deliveryAddress;


    public String getDeliveryDate() {
        return deliveryDate;
    }

    public Integer getDeliveryModeCode() {
        return deliveryModeCode;
    }

    public Integer getDeliveryOption() {
        return deliveryOption;
    }

    public Integer getPriorityDeliveryCode() {
        return priorityDeliveryCode;
    }

    public Integer getAddressType() {
        return addressType;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public OneyAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public Delivery() {
    }

    private Delivery(Delivery.Builder builder) {
        this.deliveryDate = builder.deliveryDate;
        this.deliveryModeCode = builder.deliveryModeCode;
        this.deliveryOption = builder.deliveryOption;
        this.priorityDeliveryCode = builder.priorityDeliveryCode;
        this.addressType = builder.addressType;
        this.recipient = builder.recipient;
        this.deliveryAddress = builder.deliveryAddress;
    }

    public static class Builder {
        private String deliveryDate;
        private Integer deliveryModeCode; //creer enum ?
        private Integer deliveryOption;
        private Integer priorityDeliveryCode;
        private Integer addressType; //Creer ENUM ??
        private Recipient recipient; //Creer ENUM ??
        private OneyAddress deliveryAddress;


        public static Delivery.Builder aDeliveryBuilder() {
            return new Delivery.Builder();
        }

        public Delivery.Builder withDeliveryDate(String date) {
            this.deliveryDate = date;
            return this;
        }

        public Delivery.Builder withDeliveryModeCode(Integer code) {
            this.deliveryModeCode = code;
            return this;
        }

        public Delivery.Builder withDeliveryOption(Integer option) {
            this.deliveryOption = option;
            return this;
        }

        public Delivery.Builder withPriorityDeliveryCode(Integer option) {
            this.priorityDeliveryCode = option;
            return this;
        }

        public Delivery.Builder withAddressType(Integer type) {
            this.addressType = type;
            return this;
        }

        public Delivery.Builder withRecipient(Recipient recipient) {
            this.recipient = recipient;
            return this;
        }

        public Delivery.Builder withDeliveryAddress(OneyAddress address) {
            this.deliveryAddress = address;
            return this;
        }

        private Delivery.Builder verifyIntegrity() {
            if (this.deliveryModeCode == null) {
                throw new IllegalStateException("Delivery must have a deliveryModeCode when built");
            }
            if (this.deliveryDate == null || !this.deliveryDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                throw new IllegalStateException("Delivery must have a deliveryDate in format 'yyyy-MM-dd' when built");
            }
            if (this.deliveryOption == null) {
                throw new IllegalStateException("Delivery must have a deliveryOption when built");
            }
            if (this.addressType == null) {
                throw new IllegalStateException("Delivery must have a addressType when built");
            }

            if (this.deliveryAddress == null) {
                throw new IllegalStateException("Delivery must have a deliveryAddress when built");
            }
            if (this.addressType == 5 && this.recipient == null) {
                throw new IllegalStateException("Delivery must have a recipient when built");
            }
            return this;
        }

        public Delivery.Builder fromPayline(PaymentRequest request) {
            //Todo mapping avec Oney non defini + modif  à venir avec la RC4
            this.deliveryDate = "2018-12-28";
//            this.deliveryDate = buyer.getOrder.getDeliveryExpectedDate();
            this.deliveryModeCode = 1;
//            this.deliveryModeCode =buyer.getOrder.getDeliveryMode();
            this.deliveryOption = 1;
//            this.deliveryOption = buyer.getOrder.getDeliveryTime();
            //todo mapping adressType
            this.addressType = 1;
//            this.addressType = "";
            this.recipient = Recipient.Builder.aRecipientBuilder()
                    .fromPayline(request.getBuyer()).build();
            this.deliveryAddress = OneyAddress.Builder.aOneyAddressBuilder()
                    .fromPayline(request.getBuyer(), Buyer.AddressType.DELIVERY)
                    .build();
            return this;
        }

        public Delivery build() {
            return new Delivery(this.verifyIntegrity());
        }

    }
}
