package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyAddress;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

public class Delivery extends OneyBean {

    @Required
    @SerializedName("delivery_date")
    private String deliveryDate;

    @Required
    @SerializedName("delivery_mode_code")
    private Integer deliveryModeCode; //creer enum ?

    @Required
    @SerializedName("delivery_option")
    private Integer deliveryOption;

    @SerializedName("priority_delivery_code")
    private Integer priorityDeliveryCode;

    @Required
    @SerializedName("address_type")
    private Integer addressType; //Creer ENUM ??

    private Recipient recipient;

    @Required
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

        public Delivery.Builder fromPayline(PaymentRequest request) {

            Order order = request.getOrder();
            if (order != null) {
                this.withDeliveryDate(PluginUtils.dateToString(order.getExpectedDeliveryDate()));
                if (request.getOrder() != null) {
                    this.withDeliveryModeCode(PluginUtils.getOneyDeliveryModeCode(order.getDeliveryMode()));
                    this.withDeliveryOption(PluginUtils.getOneyDeliveryOption(order.getDeliveryTime()));
                }
            }


            // set the address_type from the delivery mode code (see JIRA 175)
            switch (this.deliveryModeCode) {
                case 1:
                case 2:
                case 3:
                    this.withAddressType(this.deliveryModeCode);
                    break;
                case 4:
                    this.withAddressType(5);
                    break;
                case 5:
                    this.withAddressType(6);
                    break;
                case 6:
                    this.withAddressType(4);
                    break;
                default:
                    this.withAddressType(1);
                    break;
            }

            Buyer buyer = request.getBuyer();
            if (buyer != null) {
                this.withRecipient(Recipient.Builder.aRecipientBuilder().fromPayline(buyer).build());
                this.withDeliveryAddress(OneyAddress.Builder.aOneyAddressBuilder().fromPayline(buyer, Buyer.AddressType.DELIVERY)
                        .build());
            }
            return this;
        }

        public Delivery build() {
            return new Delivery(this);
        }

    }
}
