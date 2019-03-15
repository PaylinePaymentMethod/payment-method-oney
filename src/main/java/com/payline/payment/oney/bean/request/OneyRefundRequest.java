package com.payline.payment.oney.bean.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.PurchaseCancel;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.service.impl.RequestConfigServiceImpl;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;
import static com.payline.payment.oney.utils.PluginUtils.generateMerchantRequestId;

public class OneyRefundRequest extends ParameterizedUrlOneyRequest {

    @Expose
    @SerializedName("language_code")
    private String languageCode;

    @Required
    @Expose
    @SerializedName("merchant_request_id")
    private String merchantRequestId;

    @Required
    @Expose
    @SerializedName("purchase")
    private PurchaseCancel purchase;

    public PurchaseCancel getPurchase() {
        return purchase;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getMerchantRequestId() {
        return merchantRequestId;
    }


    public OneyRefundRequest(OneyRefundRequest.Builder builder) {
        this.purchaseReference = builder.purchaseReference;
        this.languageCode = builder.languageCode;
        this.merchantRequestId = builder.merchantRequestId;
        this.purchase = builder.purchase;
        this.encryptKey = builder.encryptKey;
        this.callParameters = builder.callParameters;
        this.merchantGuid = builder.merchantGuid;
        this.pspGuid = builder.pspGuid;
    }

    public static class Builder extends ParameterizedUrlOneyRequest.Builder {
        private String languageCode;
        private String merchantRequestId;
        private PurchaseCancel purchase;
        private String merchantGuid;
        private String pspGuid;
        private String encryptKey;
        private Map<String, String> callParameters;

        public static OneyRefundRequest.Builder aOneyRefundRequest() {
            return new OneyRefundRequest.Builder();
        }

        public Builder withPurchase(PurchaseCancel purchase) {
            this.purchase = purchase;
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

        public Builder withMerchantRequestId(String merchantGuid) {
            this.merchantRequestId = merchantGuid;
            return this;
        }

        public Builder withPspGuid(String pspGuid) {
            this.pspGuid = pspGuid;
            return this;
        }

        public Builder withPurchaseReference( String purchaseReference ){
            this.purchaseReference = purchaseReference;
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

        public OneyRefundRequest.Builder fromRefundRequest(RefundRequest refundRequest, boolean refundFlag) throws InvalidDataException {

            String merchantGuidValue = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, MERCHANT_GUID_KEY);

            this.withPurchaseReferenceFromOrder( refundRequest.getOrder() );
            this.merchantRequestId = generateMerchantRequestId(merchantGuidValue);

            this.pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, PSP_GUID_KEY);
            this.merchantGuid = merchantGuidValue;
            this.purchase = PurchaseCancel.Builder.aPurchaseCancelBuilder()
                    .withAmount(createFloatAmount(refundRequest.getAmount().getAmountInSmallestUnit(), refundRequest.getAmount().getCurrency()))
                    .withReasonCode(0)
                    .withRefundFlag(refundFlag)
                    .build();
            this.languageCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, LANGUAGE_CODE_KEY);
            this.encryptKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, PARTNER_CHIFFREMENT_KEY);
            this.callParameters = PluginUtils.getParametersMap(refundRequest);

            return this;
        }

        public OneyRefundRequest build() {
            return new OneyRefundRequest(this);
        }

    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
