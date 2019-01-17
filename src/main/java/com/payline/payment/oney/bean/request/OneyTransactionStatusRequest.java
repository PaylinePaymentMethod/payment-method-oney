package com.payline.payment.oney.bean.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.COUNTRY_CODE_KEY;

public class OneyTransactionStatusRequest extends OneyRequest {

    @SerializedName("reference")
    private String purchaseReference;
    @SerializedName("language_code")
    private String languageCode;


    public String getPurchaseReference() {
        return purchaseReference;
    }

    public String getLanguageCode() {
        return languageCode;
    }


    private OneyTransactionStatusRequest(OneyTransactionStatusRequest.Builder builder) {
        this.purchaseReference = builder.purchaseReference;
        this.languageCode = builder.languageCode;
        this.pspGuid = builder.pspGuid;
        this.merchantGuid = builder.merchantGuid;
        this.encryptKey = builder.encryptKey;
        this.callParameters = builder.callParameters;

    }

    public static class Builder {
        private String purchaseReference;
        private String languageCode;
        private String merchantGuid;
        private String pspGuid;
        private String encryptKey;
        private Map<String, String> callParameters;

        public static OneyTransactionStatusRequest.Builder aOneyGetStatusRequest() {
            return new OneyTransactionStatusRequest.Builder();
        }


        public Builder withPurchaseReference(String purchaseReference) {
            this.purchaseReference = purchaseReference;
            return this;
        }

        public Builder withLanguageCode(String languageCode) {
            this.languageCode = languageCode;
            return this;
        }

        public Builder withMerchantGuid(String merchantGuid) {
            this.merchantGuid = merchantGuid;
            return this;
        }

        public Builder withPspGuid(String pspGuid) {
            this.pspGuid = pspGuid;
            return this;
        }

        public Builder withEncryptKey(String key) {
            this.encryptKey = key;
            return this;
        }

        public Builder withCallParameters(Map<String, String> parameters) {
            this.callParameters = parameters;
            return this;
        }

        public OneyTransactionStatusRequest.Builder fromTransactionStatusRequest(TransactionStatusRequest transactionStatusRequest) {
            return OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode(transactionStatusRequest.getContractConfiguration().getProperty(OneyConstants.LANGUAGE_CODE_KEY).getValue())
                    .withMerchantGuid(transactionStatusRequest.getContractConfiguration().getProperty(OneyConstants.MERCHANT_GUID_KEY).getValue())
                    .withPspGuid(transactionStatusRequest.getContractConfiguration().getProperty(OneyConstants.PSP_GUID_KEY).getValue())
                    .withPurchaseReference(transactionStatusRequest.getTransactionId())
                    .withEncryptKey(transactionStatusRequest.getPartnerConfiguration().getProperty(OneyConstants.PARTNER_CHIFFREMENT_KEY))
                    .withCallParameters(PluginUtils.getParametersMap(
                            transactionStatusRequest.getPartnerConfiguration(),
                            transactionStatusRequest.getContractConfiguration().getProperty(COUNTRY_CODE_KEY).getValue()));


        }

        //Creer une transactionStatusRequest depuis une refund Request
        public OneyTransactionStatusRequest.Builder fromRefundRequest(RefundRequest refundRequest) {
            return OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode(refundRequest.getContractConfiguration().getProperty(OneyConstants.LANGUAGE_CODE_KEY).getValue())
                    .withMerchantGuid(refundRequest.getContractConfiguration().getProperty(OneyConstants.MERCHANT_GUID_KEY).getValue())
                    .withPspGuid(refundRequest.getContractConfiguration().getProperty(OneyConstants.PSP_GUID_KEY).getValue())
                    .withPurchaseReference(refundRequest.getTransactionId())
                    .withEncryptKey(refundRequest.getPartnerConfiguration().getProperty(OneyConstants.PARTNER_CHIFFREMENT_KEY))
                    .withCallParameters(PluginUtils.getParametersMap(
                            refundRequest.getPartnerConfiguration(),
                            refundRequest.getContractConfiguration().getProperty(COUNTRY_CODE_KEY).getValue()));


        }

        private OneyTransactionStatusRequest.Builder verifyIntegrity() {
            if (this.merchantGuid == null) {
                throw new IllegalStateException("OneyTransactionStatusRequest must have a merchantGuid when built");
            }

            if (this.pspGuid == null) {
                throw new IllegalStateException("OneyTransactionStatusRequest must have a pspGuid when built");
            }

            if (this.purchaseReference == null) {
                throw new IllegalStateException("OneyTransactionStatusRequest must have a reference when built");
            }

            if (this.encryptKey == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a encryptKey when built");
            }

            if (this.callParameters == null || callParameters.isEmpty()) {
                throw new IllegalStateException("OneyTransactionStatusRequest must have a callParameters when built");
            }

            return this;

        }

        public OneyTransactionStatusRequest build() {
            return new OneyTransactionStatusRequest(this.verifyIntegrity());
        }

    }


}
