package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.common.Buyer;

import java.util.List;

import static com.payline.payment.oney.utils.PluginUtils.*;

public class OneyAddress extends OneyBean {

    @Required
    private String line1;

    private String line2;

    private String line3;

    private String line4;

    private String line5;

    @Required
    @SerializedName("postal_code")
    private String postalCode;

    @Required
    private String municipality;

    @Required
    @SerializedName("country_code")
    private String countryCode;

    @Required
    @SerializedName("country_label")
    private String countryLabel;

    @SerializedName("arrondissement_code")
    private Integer arrondissementCode;


    public String getLine1() {
        return line1;
    }

    public String getLine2() {
        return line2;
    }

    public String getLine3() {
        return line3;
    }

    public String getLine4() {
        return line4;
    }

    public String getLine5() {
        return line5;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getMunicipality() {
        return municipality;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryLabel() {
        return countryLabel;
    }

    public Integer getArrondissementCode() {
        return arrondissementCode;
    }

    private OneyAddress() {
    }

    private OneyAddress(OneyAddress.Builder builder) {
        this.line1 = builder.line1;
        this.line2 = builder.line2;
        this.line3 = builder.line3;
        this.line4 = builder.line4;
        this.line5 = builder.line5;
        this.postalCode = builder.postalCode;
        this.municipality = builder.municipality;
        this.countryCode = builder.countryCode;
        this.countryLabel = builder.countryLabel;
        this.arrondissementCode = builder.arrondissementCode;
    }

    public static class Builder {
        private String line1;
        private String line2;
        private String line3;
        private String line4;
        private String line5;
        private String postalCode;
        private String municipality;
        private String countryCode;
        private String countryLabel;
        private Integer arrondissementCode;


        public static OneyAddress.Builder aOneyAddressBuilder() {
            return new OneyAddress.Builder();
        }

        public OneyAddress.Builder withLine1(String line) {
            this.line1 = line;
            return this;
        }

        public OneyAddress.Builder withLine2(String line) {
            this.line2 = line;
            return this;
        }

        public OneyAddress.Builder withLine3(String line) {
            this.line3 = line;
            return this;
        }

        public OneyAddress.Builder withLine4(String line) {
            this.line4 = line;
            return this;
        }

        public OneyAddress.Builder withLine5(String line) {
            this.line5 = line;
            return this;
        }

        public OneyAddress.Builder withPostalCode(String code) {
            this.postalCode = code;
            return this;
        }

        public OneyAddress.Builder withMunicipality(String municipality) {
            this.municipality = municipality;
            return this;
        }

        public OneyAddress.Builder withCountryCode(String code) {
            this.countryCode = code;
            return this;
        }

        public OneyAddress.Builder withCountryLabel(String label) {
            this.countryLabel = label;
            return this;
        }

        public OneyAddress.Builder withArrondissmentCode(Integer code) {
            this.arrondissementCode = code;
            return this;
        }

        public Builder fromPayline(Buyer buyer, Buyer.AddressType addressType) {
            if (buyer == null) {
                return null;
            }

            Buyer.Address address = buyer.getAddressForType(addressType);
            if (address != null) {
                this.truncateAddress(address.getStreet1(), address.getStreet2());

                this.withMunicipality(address.getCity());
                this.withPostalCode(address.getZipCode());
                this.withCountryLabel(getCountryNameCodeFromCountryCode2(address.getCountry()));
                this.withCountryCode(getIsoAlpha3CodeFromCountryCode2(address.getCountry()));
            }

            return this;
        }

        // Découpe l'adresse intelligemment
        private void truncateAddress(String street1, String street2) {
            List<String> addressTruncated = splitLongText(spaceConcat(street1, street2), 38);
            int nbLines = addressTruncated.size();

            if (nbLines >= 1) {
                this.line1 = addressTruncated.get(0);
            }
            if (nbLines >= 2) {
                this.line2 = addressTruncated.get(1);
            }
            if (nbLines >= 3) {
                this.line3 = addressTruncated.get(2);
            }
            if (nbLines >= 4) {
                this.line4 = addressTruncated.get(3);
            }
            if (nbLines >= 5) {
                this.line5 = addressTruncated.get(4);
            }
        }

        public OneyAddress build() {
            return new OneyAddress(this);
        }
    }

}
