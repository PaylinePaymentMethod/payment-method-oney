package com.payline.payment.oney.bean.response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.PurchaseNotification;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.exception.MalformedJsonException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.stream.Stream;

import static com.payline.payment.oney.utils.OneyConstants.CHIFFREMENT_IS_ACTIVE;

public class OneyNotificationResponse extends OneyResponse {
    private static final Logger LOGGER = LogManager.getLogger(OneyNotificationResponse.class);

    @SerializedName("language_code")
    private String languageCode;

    @SerializedName("merchant_guid")
    private String merchantGuid;

    @SerializedName("oney_request_id")
    private String oneyRequestId;

    @SerializedName("purchase")
    private PurchaseNotification purchase;

    @SerializedName("customer")
    private Customer customer;

    @SerializedName("psp_context")
    private String pspContext;

    @SerializedName("merchant_context")
    private String merchantContext;

    public String getLanguageCode() {
        return languageCode;
    }

    public String getMerchantGuid() {
        return merchantGuid;
    }

    public String getOneyRequestId() {
        return oneyRequestId;
    }

    public PurchaseNotification getPurchase() {
        return purchase;
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getPspContext() {
        return pspContext;
    }

    public String getMerchantContext() {
        return merchantContext;
    }

    public boolean isEmpty() {
        return Stream.of(languageCode, merchantGuid, oneyRequestId, purchase, customer, pspContext, merchantContext)
                .allMatch(Objects::isNull);
    }

    public static OneyNotificationResponse createTransactionStatusResponseFromJson(String json, String encryptKey)
            throws PluginTechnicalException {
        Gson parser = new Gson();

        OneyNotificationResponse oneyNotificationResponse;
        try {
            oneyNotificationResponse = parser.fromJson(json, OneyNotificationResponse.class);
        } catch (JsonSyntaxException e) {
            LOGGER.error("Unable to parse JSON content", e);
            throw new MalformedJsonException(e);
        }

        // JSON was properly formed, but there was some unexpected field()s in the content.
        if (oneyNotificationResponse.isEmpty()) {
            throw new MalformedJsonException("Unable to parse JSON as OneyNotificationResponse");
        }

        //Cas reponse est chiffree : on dechiffre la reponse afin de recuperer le statut de la transaction
        if (Boolean.parseBoolean(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            String decryptedMessage = OneyResponse.decryptMessage(oneyNotificationResponse.getEncryptedMessage(), encryptKey);

            oneyNotificationResponse = parser.fromJson(decryptedMessage, OneyNotificationResponse.class);

        }

        // Check oney response integity
        if (oneyNotificationResponse.purchase == null) {
            throw new InvalidDataException("Purchase must not be null", "purchase");
        } else if (oneyNotificationResponse.customer == null) {
            throw new InvalidDataException("Customer must not be null", "customer");

        }
        return oneyNotificationResponse;
    }

}
