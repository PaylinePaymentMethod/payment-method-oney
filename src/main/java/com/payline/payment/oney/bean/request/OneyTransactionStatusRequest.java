package com.payline.payment.oney.bean.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.service.impl.RequestConfigServiceImpl;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.util.Map;

public class OneyTransactionStatusRequest extends ParameterizedUrlOneyRequest {

    @SerializedName("language_code")
    private String languageCode;
    
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

    public static class Builder extends ParameterizedUrlOneyRequest.Builder {
        private String languageCode;
        private String merchantGuid;
        private String pspGuid;
        private String encryptKey;
        private Map<String, String> callParameters;

        public static OneyTransactionStatusRequest.Builder aOneyGetStatusRequest() {
            return new OneyTransactionStatusRequest.Builder();
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

        public Builder withPurchaseReference(String purchaseReference ){
            this.purchaseReference = purchaseReference;
            return this;
        }

        public Builder withPurchaseReferenceFromOrder( Order order ){
            super.withPurchaseReferenceFromOrder( order );
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

        public OneyTransactionStatusRequest.Builder fromTransactionStatusRequest(TransactionStatusRequest transactionStatusRequest) throws InvalidDataException {
            return OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode(RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, OneyConstants.LANGUAGE_CODE_KEY))
                    .withMerchantGuid(RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, OneyConstants.MERCHANT_GUID_KEY))
                    .withPspGuid(RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, OneyConstants.PSP_GUID_KEY))
                    .withPurchaseReferenceFromOrder( transactionStatusRequest.getOrder() )
                    .withEncryptKey(RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, OneyConstants.PARTNER_CHIFFREMENT_KEY))
                    .withCallParameters(PluginUtils.getParametersMap(transactionStatusRequest));


        }

        //Creer une transactionStatusRequest depuis une refund Request
        public OneyTransactionStatusRequest.Builder fromRefundRequest(RefundRequest refundRequest) throws InvalidDataException {
            return OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                    .withLanguageCode(RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, OneyConstants.LANGUAGE_CODE_KEY))
                    .withMerchantGuid(RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, OneyConstants.MERCHANT_GUID_KEY))
                    .withPspGuid(RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, OneyConstants.PSP_GUID_KEY))
                    .withPurchaseReferenceFromOrder( refundRequest.getOrder() )
                    .withEncryptKey(RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, OneyConstants.PARTNER_CHIFFREMENT_KEY))
                    .withCallParameters(PluginUtils.getParametersMap(refundRequest));
        }

        public OneyTransactionStatusRequest build() {
            return new OneyTransactionStatusRequest(this);
        }

    }

}
