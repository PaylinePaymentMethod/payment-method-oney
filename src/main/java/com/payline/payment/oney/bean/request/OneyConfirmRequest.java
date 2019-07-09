package com.payline.payment.oney.bean.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.response.OneyNotificationResponse;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.service.impl.RequestConfigServiceImpl;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.Required;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;

import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;
import static com.payline.payment.oney.utils.PluginUtils.generateMerchantRequestId;

public class OneyConfirmRequest extends ParameterizedUrlOneyRequest {

    //RequestBody
    @Expose
    @SerializedName("language_code")
    private String languageCode;

    @Required
    @Expose
    @SerializedName("merchant_request_id")
    private String merchantRequestId;

    @Required
    @Expose
    @SerializedName("payment")
    private PaymentData paymentData;

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

    public static class Builder extends ParameterizedUrlOneyRequest.Builder {
        private String languageCode;
        private String merchantRequestId;
        private PaymentData paymentData;
        private String merchantGuid;
        private String pspGuid;
        private String encryptKey;
        private Map<String, String> callParameters;

        public Builder(RedirectionPaymentRequest paymentRequest) throws InvalidDataException {
            String merchantGuidValue = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, MERCHANT_GUID_KEY);
            this.purchaseReference = paymentRequest.getRequestContext().getRequestData().get(EXTERNAL_REFERENCE_KEY);
            this.languageCode = paymentRequest.getRequestContext().getRequestData().get(LANGUAGE_CODE_KEY);
            this.merchantRequestId = generateMerchantRequestId(merchantGuidValue);

            this.pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PSP_GUID_KEY);
            this.merchantGuid = merchantGuidValue;
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(createFloatAmount(paymentRequest.getAmount().getAmountInSmallestUnit(), paymentRequest.getAmount().getCurrency()))
                    .buildForConfirmRequest();
            this.encryptKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PARTNER_CHIFFREMENT_KEY);
            this.callParameters = PluginUtils.getParametersMap(paymentRequest);
        }


        public Builder(TransactionStatusRequest transactionStatusRequest) throws InvalidDataException {
            String merchantGuidValue = RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, MERCHANT_GUID_KEY);

            this.withPurchaseReferenceFromOrder(transactionStatusRequest.getOrder());
            this.merchantRequestId = generateMerchantRequestId(merchantGuidValue);

            this.pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, PSP_GUID_KEY);
            this.merchantGuid = merchantGuidValue;
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(createFloatAmount(transactionStatusRequest.getAmount().getAmountInSmallestUnit(), transactionStatusRequest.getAmount().getCurrency()))
                    .buildForConfirmRequest();
            this.encryptKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, PARTNER_CHIFFREMENT_KEY);
            this.callParameters = PluginUtils.getParametersMap(transactionStatusRequest);
        }

        public Builder(CaptureRequest captureRequest) throws InvalidDataException {
            String merchantGuidValue = RequestConfigServiceImpl.INSTANCE.getParameterValue(captureRequest, MERCHANT_GUID_KEY);

            this.withPurchaseReferenceFromOrder(captureRequest.getOrder());
            this.merchantRequestId = generateMerchantRequestId(merchantGuidValue);

            this.pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(captureRequest, PSP_GUID_KEY);
            this.merchantGuid = merchantGuidValue;
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(createFloatAmount(captureRequest.getAmount().getAmountInSmallestUnit(), captureRequest.getAmount().getCurrency()))
                    .buildForConfirmRequest();
            this.encryptKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(captureRequest, PARTNER_CHIFFREMENT_KEY);
            this.callParameters = PluginUtils.getParametersMap(captureRequest);
        }

        public Builder(NotificationRequest notificationRequest, OneyNotificationResponse oneyResponse) throws InvalidDataException {
            this.languageCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, LANGUAGE_CODE_KEY);
            this.merchantGuid = oneyResponse.getMerchantGuid();
            this.merchantRequestId = generateMerchantRequestId(this.merchantGuid);
            this.paymentData = PaymentData.Builder.aPaymentData()
                    .withAmount(PluginUtils.getAmount(oneyResponse.getMerchantContext()))
                    .withCurrency(PluginUtils.getCurrency(oneyResponse.getMerchantContext()))
                    .buildForConfirmRequest();
            this.encryptKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, PARTNER_CHIFFREMENT_KEY);


            this.purchaseReference = OneyConstants.EXTERNAL_REFERENCE_TYPE + OneyConstants.PIPE + oneyResponse.getPurchase().getExternalReference();
            this.pspGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, PSP_GUID_KEY);
            this.callParameters = PluginUtils.getParametersMap(notificationRequest);
        }

        public OneyConfirmRequest build() {
            return new OneyConfirmRequest(this);
        }

    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

}
