package com.payline.payment.oney.bean.common;

import com.google.gson.annotations.SerializedName;
import com.payline.pmapi.bean.common.Buyer;

import java.util.Map;

import static com.payline.payment.oney.utils.PluginUtils.*;

public class OneyAddress extends OneyBean {

    private String line1;
    private String line2;
    private String line3;
    private String line4;
    private String line5;
    @SerializedName("postal_code")
    private String postalCode;
    private String municipality;
    @SerializedName("country_code")
    private String countryCode;
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

        private OneyAddress.Builder checkIntegrity() {
            if (this.line1 == null) {
                throw new IllegalStateException("OneyAddress must have a line1 when built");
            }
            if (this.countryLabel == null) {
                throw new IllegalStateException("OneyAddress must have a countryLabel when built");
            }
            if (this.postalCode == null) {
                throw new IllegalStateException("OneyAddress must have a postalCode when built");
            }
            if (this.municipality == null) {
                throw new IllegalStateException("OneyAddress must have a municipality when built");
            }
            if (this.countryCode == null) {
                throw new IllegalStateException("OneyAddress must have a countryCode when built");
            }
            return this;

        }

        public Builder fromPayline(Buyer buyer, Buyer.AddressType addressType) {

            String street = buyer.getAddressForType(addressType).getStreet1();
            String street2 = buyer.getAddressForType(addressType).getStreet2();
            this.truncateAddress(street, street2);

            this.municipality = buyer.getAddressForType(addressType).getCity();
            this.postalCode = buyer.getAddressForType(addressType).getZipCode();
            this.countryLabel = getCountryNameCodeFromCountryCode2(buyer.getAddressForType(addressType).getCountry());
            this.countryCode = getIsoAlpha3CodeFromCountryCode2(buyer.getAddressForType(addressType).getCountry());

            return this;
        }

        // Découpe l'adresse intelligemment
        private void truncateAddress(String street1, String street2) {
            Map addressTruncated = truncateLongText(street1, street2, 38);

            if (addressTruncated.get("line1") != null) {
                this.line1 = addressTruncated.get("line1").toString();
            }
            if (addressTruncated.get("line2") != null) {
                this.line2 = addressTruncated.get("line2").toString();
            }
            if (addressTruncated.get("line3") != null) {
                this.line3 = addressTruncated.get("line3").toString();
            }
            if (addressTruncated.get("line4") != null) {
                this.line4 = addressTruncated.get("line4").toString();
            }
            if (addressTruncated.get("line5") != null) {
                this.line5 = addressTruncated.get("line5").toString();
            }
        }

        public OneyAddress build() {
            return new OneyAddress(this.checkIntegrity());
        }
    }

}
