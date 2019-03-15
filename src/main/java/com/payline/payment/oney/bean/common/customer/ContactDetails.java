package com.payline.payment.oney.bean.common.customer;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.common.Buyer;

public class ContactDetails extends OneyBean {

    @Required
    @SerializedName("landline_number")
    private String landLineNumber;

    @Required
    @SerializedName("mobile_phone_number")
    private String mobilePhoneNumber;

    @SerializedName("fax_number")
    private String faxNumber;

    @Required
    @SerializedName("email_address")
    private String emailAdress;

    public String getLandLineNumber() {
        return landLineNumber;
    }

    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    private ContactDetails(){
    }

    private ContactDetails(ContactDetails.Builder builder) {
        this.landLineNumber = builder.landLineNumber;
        this.mobilePhoneNumber = builder.mobilePhoneNumber;
        this.faxNumber = builder.faxNumber;
        this.emailAdress = builder.emailAdress;
    }

    public static class Builder {
        private String landLineNumber;
        private String mobilePhoneNumber;
        private String faxNumber;
        private String emailAdress;

        public static ContactDetails.Builder aContactDetailsBuilder() {
            return new ContactDetails.Builder();
        }

        public ContactDetails.Builder withLandLineNumber(String number) {
            this.landLineNumber = number;
            return this;
        }

        public ContactDetails.Builder withMobilePhoneNumber(String number) {
            this.mobilePhoneNumber = number;
            return this;
        }

        public ContactDetails.Builder withFaxNumber(String faxNumber) {
            this.faxNumber = faxNumber;
            return this;
        }

        public ContactDetails.Builder withEmailAdress(String address) {
            this.emailAdress = address;
            return this;
        }

        public ContactDetails.Builder fromPayline(Buyer buyer) {
            if (buyer == null) {
                return null;
            }

            final String phoneNumber = buyer.getPhoneNumberForType(Buyer.PhoneNumberType.CELLULAR);
            this.mobilePhoneNumber = phoneNumber;
            this.landLineNumber = phoneNumber;
            // this.faxNumber
            this.emailAdress = buyer.getEmail();
            return this;
        }

        public ContactDetails build() {
            return new ContactDetails(this);
        }

    }
}
