package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.common.Buyer;

public class Recipient extends OneyBean {

    @Required
    @SerializedName("recipient_honorific_code")
    private Integer honorificCode;

    @Required
    private String surname;

    @Required
    @SerializedName("first_name")
    private String firstName;

    @Required
    @SerializedName("phone_number")
    private String phoneNumber;

    public Integer getHonorificCode() {
        return honorificCode;
    }

    public String getSurname() {
        return surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    private Recipient() {
    }

    private Recipient(Recipient.Builder builder) {
        this.honorificCode = builder.honorificCode;
        this.surname = builder.surname;
        this.firstName = builder.firstName;
        this.phoneNumber = builder.phoneNumber;
    }

    public static class Builder {
        private Integer honorificCode;
        private String surname;
        private String firstName;
        private String phoneNumber;

        public static Recipient.Builder aRecipientBuilder() {
            return new Recipient.Builder();
        }

        public Recipient.Builder withHonorificCode(Integer code) {
            this.honorificCode = code;
            return this;
        }

        public Recipient.Builder withSurname(String name) {
            this.surname = name;
            return this;
        }

        public Recipient.Builder withFirstname(String name) {
            this.firstName = name;
            return this;
        }

        public Recipient.Builder withPhoneNumber(String number) {
            this.phoneNumber = number;
            return this;
        }

        public Recipient.Builder fromPayline(Buyer buyer) {
            if (buyer == null) {
                return null;
            }

            Buyer.Address deliveryAddress = buyer.getAddressForType(Buyer.AddressType.DELIVERY);
            if (deliveryAddress != null && deliveryAddress.getFullName() != null) {
                Buyer.FullName fullName = deliveryAddress.getFullName();
                this.withSurname(fullName.getLastName());
                this.withFirstname(fullName.getFirstName());
                this.withHonorificCode(PluginUtils.getHonorificCode(fullName.getCivility()));
            }
            this.withPhoneNumber( buyer.getPhoneNumberForType(Buyer.PhoneNumberType.BILLING));

            return this;
        }

        public Recipient build() {
            return new Recipient(this);
        }
    }
}
