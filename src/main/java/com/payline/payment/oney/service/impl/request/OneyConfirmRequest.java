package com.payline.payment.oney.service.impl.request;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseRedirect;

import static com.payline.payment.oney.utils.OneyConstants.MERCHANT_GUID_KEY;
import static com.payline.payment.oney.utils.OneyConstants.PSP_GUID_KEY;

public class OneyConfirmRequest extends OneyRequest {

    @SerializedName("reference")
    private String purchaseReference;
    //RequestBody
    @SerializedName("language_code")
    private String languageCode;
    @SerializedName("merchant_request_id")
    private String merchantRequestId;
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


    private OneyConfirmRequest(OneyConfirmRequest.Builder builder) {
        this.purchaseReference = builder.purchaseReference;
        this.languageCode = builder.languageCode;
        this.merchantRequestId = builder.merchantRequestId;
        this.paymentData = builder.paymentData;
        this.pspGuid = builder.pspGuid;
        this.merchantGuid = builder.merchantGuid;


    }

    public static class Builder {
        private String purchaseReference;
        private String languageCode;
        private String merchantRequestId;
        private PaymentData paymentData;
        private String merchantGuid;
        private String pspGuid;

        public static OneyConfirmRequest.Builder aOneyConfirmRequest() {
            return new OneyConfirmRequest.Builder();
        }


        //utile ??
        public OneyConfirmRequest.Builder fromPaylineResponseRedirect(PaymentResponseRedirect paymentResponseRedirect) {
            this.purchaseReference = paymentResponseRedirect.getRequestContext().getRequestData().get(OneyConstants.EXTERNAL_REFERENCE_KEY);
            this.languageCode = paymentResponseRedirect.getRequestContext().getRequestData().get(OneyConstants.LANGUAGE_CODE_KEY);
            this.merchantRequestId = paymentResponseRedirect.getPartnerTransactionId();
            this.pspGuid = paymentResponseRedirect.getRequestContext().getRequestData().get(OneyConstants.PSP_GUID_KEY);
            this.merchantGuid = paymentResponseRedirect.getRequestContext().getRequestData().get(OneyConstants.MERCHANT_GUID_KEY);
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(Float.parseFloat(paymentResponseRedirect.getRequestContext().getRequestData().get(OneyConstants.PAYMENT_AMOUNT_KEY)))
                    .buildForConfirmRequest();
            return this;
        }

        //Not sure
        public OneyConfirmRequest.Builder fromPaylineRedirectionPaymentRequest(RedirectionPaymentRequest paymentRequest) {
            this.purchaseReference = paymentRequest.getRequestContext().getRequestData().get(OneyConstants.EXTERNAL_REFERENCE_KEY);
            this.languageCode = paymentRequest.getLocale().getLanguage();
            this.merchantRequestId = paymentRequest.getTransactionId();

            this.pspGuid = paymentRequest.getPartnerConfiguration().getProperty(PSP_GUID_KEY);
            this.merchantGuid = paymentRequest.getContractConfiguration().getProperty(MERCHANT_GUID_KEY).getValue();
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(paymentRequest.getOrder().getAmount().getAmountInSmallestUnit().floatValue())
                    .buildForConfirmRequest();
            return this;
        }

        private OneyConfirmRequest.Builder verifyIntegrity() {
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
            } else {
                return this;
            }

        }

        public OneyConfirmRequest build() {
            return new OneyConfirmRequest(this.verifyIntegrity());
        }

    }


}