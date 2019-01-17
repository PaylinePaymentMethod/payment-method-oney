package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.common.Buyer;

public class Recipient extends OneyBean {

    @SerializedName("recipient_honorific_code")
    private Integer honorificCode;
    private String surname;
    @SerializedName("first_name")
    private String firstName;
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
            this.surname = buyer.getAddressForType(Buyer.AddressType.DELIVERY).getFullName().getLastName();
            this.firstName = buyer.getAddressForType(Buyer.AddressType.DELIVERY).getFullName().getFirstName();
            this.phoneNumber = buyer.getPhoneNumberForType(Buyer.PhoneNumberType.BILLING);
            this.honorificCode = PluginUtils.getHonorificCode(buyer.getAddressForType(Buyer.AddressType.DELIVERY).getFullName().getCivility());
            return this;
        }

        private Recipient.Builder verifyIntegrity() {
            if (this.surname == null) {
                throw new IllegalStateException("Recipient must have a surname when built");
            }
            if (this.firstName == null) {
                throw new IllegalStateException("Recipient must have a firstName when built");
            }
            return this;

        }

        public Recipient build() {
            return new Recipient(this.verifyIntegrity());
        }
    }
}
