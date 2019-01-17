package com.payline.payment.oney.bean.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;

import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;
import static com.payline.payment.oney.utils.PluginUtils.generateMerchantRequestId;

public class OneyConfirmRequest extends OneyRequest {

    @SerializedName("reference")
    private String purchaseReference;
    //RequestBody
    @Expose
    @SerializedName("language_code")
    private String languageCode;
    @Expose
    @SerializedName("merchant_request_id")
    private String merchantRequestId;
    @Expose
    @SerializedName("payment")
    private PaymentData paymentData;

    public String getPurchaseReference() {
        return purchaseReference;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getMerchantRequestId() {
        return merchantRequestId;
    }

    public PaymentData getPaymentData() {
        return paymentData;
    }


    private OneyConfirmRequest(Builder builder) {
        this.purchaseReference = builder.purchaseReference;
        this.languageCode = builder.languageCode;
        this.merchantRequestId = builder.merchantRequestId;
        this.paymentData = builder.paymentData;
        this.pspGuid = builder.pspGuid;
        this.merchantGuid = builder.merchantGuid;
        this.encryptKey = builder.encryptKey;
        this.callParameters = builder.callParameters;


    }

    public static class Builder {
        private String purchaseReference;
        private String languageCode;
        private String merchantRequestId;
        private PaymentData paymentData;
        private String merchantGuid;
        private String pspGuid;
        private String encryptKey;
        private Map<String, String> callParameters;

        public Builder(RedirectionPaymentRequest paymentRequest) {
            String merchantGuidValue = paymentRequest.getContractConfiguration().getProperty(MERCHANT_GUID_KEY).getValue();
            this.purchaseReference = paymentRequest.getRequestContext().getRequestData().get(EXTERNAL_REFERENCE_KEY);
            this.languageCode = paymentRequest.getLocale().getLanguage();
            this.merchantRequestId = generateMerchantRequestId(merchantGuidValue);

            this.pspGuid = paymentRequest.getPartnerConfiguration().getProperty(PSP_GUID_KEY);
            this.merchantGuid = merchantGuidValue;
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(createFloatAmount(paymentRequest.getAmount().getAmountInSmallestUnit(),paymentRequest.getAmount().getCurrency()))
                    .buildForConfirmRequest();
            this.encryptKey = paymentRequest.getPartnerConfiguration().getProperty(PARTNER_CHIFFREMENT_KEY);
            this.callParameters = PluginUtils.getParametersMap(
                    paymentRequest.getPartnerConfiguration(),
                    paymentRequest.getContractConfiguration().getProperty(COUNTRY_CODE_KEY).getValue());
        }


        public Builder(TransactionStatusRequest transactionStatusRequest) {
            String merchantGuidValue = transactionStatusRequest.getContractConfiguration().getProperty(MERCHANT_GUID_KEY).getValue();

            this.purchaseReference = transactionStatusRequest.getTransactionId();
            this.merchantRequestId = generateMerchantRequestId(merchantGuidValue);

            this.pspGuid = transactionStatusRequest.getPartnerConfiguration().getProperty(PSP_GUID_KEY);
            this.merchantGuid = merchantGuidValue;
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(createFloatAmount(transactionStatusRequest.getAmount().getAmountInSmallestUnit(),transactionStatusRequest.getAmount().getCurrency()))
                    .buildForConfirmRequest();
            this.encryptKey = transactionStatusRequest.getPartnerConfiguration().getProperty(PARTNER_CHIFFREMENT_KEY);
            this.callParameters = PluginUtils.getParametersMap(
                    transactionStatusRequest.getPartnerConfiguration(),
                    transactionStatusRequest.getContractConfiguration().getProperty(COUNTRY_CODE_KEY).getValue());
        }

        public OneyConfirmRequest build() {
            this.verifyIntegrity();
            return new OneyConfirmRequest(this);
        }


        private void verifyIntegrity() {

            if (this.merchantGuid == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a merchantGuid when built");
            }

            if (this.merchantRequestId == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a merchantRequestId when built");
            }

            if (this.pspGuid == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a pspGuid when built");
            }

            if (this.purchaseReference == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a reference when built");
            }

            if (this.paymentData == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a paymentData when built");
            }

            if (this.encryptKey == null) {
                throw new IllegalStateException("OneyConfirmRequest must have a encryptKey when built");
            }

            if (this.callParameters == null || callParameters.isEmpty()) {
                throw new IllegalStateException("OneyConfirmRequest must have a callParameters when built");
            }

        }


    }

    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

}
