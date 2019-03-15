package com.payline.payment.oney.bean.common.payment;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.exception.InvalidFieldFormatException;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.payment.ContractConfiguration;

import static com.payline.payment.oney.utils.OneyConstants.OPC_KEY;

public class BusinessTransactionData extends OneyBean {


    @Required
    private String code;

    private Integer version;

    @SerializedName("business_transaction_type")
    private String businessTransactionType; //do enum ?

    public String getCode() {
        return code;
    }

    public Integer getVersion() {
        return version;
    }

    public String getBusinessTransactionType() {
        return businessTransactionType;
    }

    private BusinessTransactionData() {
    }

    private BusinessTransactionData(BusinessTransactionData.Builder builder) {
        this.code = builder.code;
        this.version = builder.version;
        this.businessTransactionType = builder.businessTransactionType;
    }

    public static class Builder {
        private String code;
        private Integer version;
        private String businessTransactionType; //do enum ?

        public static BusinessTransactionData.Builder aBusinessTransactionDataBuilder() {
            return new BusinessTransactionData.Builder();
        }


        public BusinessTransactionData.Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public BusinessTransactionData.Builder withVersion(Integer version) {
            this.version = version;
            return this;
        }

        public BusinessTransactionData.Builder withBusinessTransactionType(String btt) {
            this.businessTransactionType = btt;
            return this;
        }

        public BusinessTransactionData.Builder fromPayline(ContractConfiguration contract) throws InvalidFieldFormatException {

            if (contract == null) {
                return null;
            }

            if (contract.getProperty(OPC_KEY) == null) {
                throw new InvalidFieldFormatException("Property " + OPC_KEY + " doesn't exists", OPC_KEY);
            }

            this.code = contract.getProperty(OPC_KEY).getValue();

            //optional ajouter dans les contract configuration  v2 ??
            /**
             this.version = BUSINESS_TRANSACTION_VERSION;
             this.businessTransactionType = BUSINESS_TRANSACTION_TYPE;
             */
            return this;
        }

        public BusinessTransactionData build() {
            return new BusinessTransactionData(this);
        }
    }
}
