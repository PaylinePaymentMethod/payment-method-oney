package com.payline.payment.oney.utils;

public class OneyConstants {

    private OneyConstants() {
    }


    public static final String I18N_SERVICE_DEFAULT_LOCALE = "en";
    public static final String RESOURCE_BUNDLE_BASE_NAME = "messages";

    //Headers
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_VALUE = "application/json";
    public static final String AUTHORIZATION = "X-Oney-Authorization";
    public static final String SECRET_KEY = "X-Oney-Secret";
    public static final String SECRET_VALUE = "Method-body";

    public static final String CONFIG_HTTP_CONNECT_TIMEOUT = "http.connectTimeout";
    public static final String CONFIG_HTTP_WRITE_TIMEOUT = "http.writeTimeout";
    public static final String CONFIG_HTTP_READ_TIMEOUT = "http.readTimeout";


    //Constant specific of this payment method

    public static final String X_ONEY_AUTHORIZATION_LABEL = "api.key.label";
    public static final String X_ONEY_AUTHORIZATION_KEY = "apiKey";
    public static final String X_ONEY_AUTHORIZATION_DESCRIPTION = "api.key.description";
    public static final String X_ONEY_AUTHORIZATION_MESSAGE_ERROR = "api.key.error.message";

    public static final String PSP_GUID_LABEL = "psp.id.label";
    public static final String PSP_GUID_KEY = "pspId";
    public static final String PSP_GUID_DESCRIPTION = "psp.id.description";
    public static final String PSP_GUID_MESSAGE_ERROR = "psp.id.error.message";

    public static final String MERCHANT_GUID_KEY = "merchantGuid";
    public static final String MERCHANT_GUID_LABEL = "merchant.guid.label";
    public static final String MERCHANT_GUID_DESCRIPTION = "merchant.guid.description";
    public static final String MERCHANT_GUID_MESSAGE_ERROR = "merchant.guid.error.message";

    public static final String API_MARKETING_KEY = "apiMarketing";
    public static final String API_MARKETING_LABEL = "api.marketing.label";
    public static final String API_MARKETING_DESCRIPTION = "api.marketing.description";
    public static final String API_MARKETING_MESSAGE_ERROR = "api.marketing.error.message";

    public static final String OPC_KEY = "opc";
    public static final String OPC_LABEL = "opc.label";
    public static final String OPC_DESCRIPTION = "opc.description";
    public static final String OPC_MESSAGE_ERROR = "opc.error.message";

    public static final String NB_ECHEANCES_KEY = "nbEcheances";
    public static final String NB_ECHEANCES_LABEL = "nb.echeances.label";
    public static final String NB_ECHEANCES_MESSAGE_ERROR = "nb.echeances.error.message";

    public static final String COUNTRY_CODE_KEY = "X-Oney-Partner-Country-Code";
    public static final String COUNTRY_CODE_VALUE = "country.code.value";
    public static final String COUNTRY_CODE_LABEL = "country.code.label";
    public static final String COUNTRY_CODE_DESCRIPTION = "X-Oney-Partner-Country-Code";
    public static final String COUNTRY_CODE_MESSAGE_ERROR = "country.code.error.message";

    public static final String LANGUAGE_CODE_KEY = "merchant_language_code";
    public static final String LANGUAGE_CODE_DESCRIPTION = "language.code.description";
    public static final String LANGUAGE_CODE_LABEL = "language.code.label";
    public static final String LANGUAGE_CODE_MESSAGE_ERROR = "language.code.error.message";


    public static final String ID_INTERNATIONAL_KEY = "oney_international_id";
    public static final String ID_INTERNATIONAL_DESCRIPTION = "X-Oney-International-Partner-ID";
    public static final String ID_INTERNATIONAL_LABEL = "international.code.label";
    public static final String ID_INTERNATIONAL_MESSAGE_ERROR = "international.code.error.message";


    public static final String PARTNER_AUTHORIZATION_VALUE = "oney.authorization";
    public static final String PARTNER_CHIFFREMENT_KEY = "partner.oney.access.secret.key";
    public static final String PARTNER_AUTHRIZATION_KEY = "partner.oney.access.key";
    public static final String PARTNER_API_URL = "partner.oney.url";
    public static final String HEADER_COUNTRY_CODE = "oney.coutry.code";


    public static final String PIPE = "%7C";


    //Request URL's
    public static final String PAYMENT_REQUEST_URL = "/staging/payments/v1/purchase/facilypay_url";
    public static final String CONFIRM_REQUEST_URL = "/staging/payments/v1/purchase";
    public static final String CANCEL_REQUEST_URL = "/staging/payments/v1/purchase";
    public static final String STATUS_REQUEST_URL = "/staging/payments/v1/purchase";

    //0 immediate, 1 deferred, 2 check card
    public static final int PAYMENT_TYPE = 0;

    // Paramètre optionnel, n'est pas utilisé dans la version actuelle
    public static final int BUSINESS_TRANSACTION_VERSION = 1;
    //PNFCB / AFFECTE / NONAMORTISSABLE
    public static final String BUSINESS_TRANSACTION_TYPE = "PNFCB";

    public static final String EXTERNAL_REFERENCE_TYPE = "CMDE";
    public static final String EXTERNAL_REFERENCE_KEY = "externalReference";
    public static final String PAYMENT_AMOUNT_KEY = "paymentAmount";

    //Code HTTP
    public static final int HTTP_OK = 200;


    public static final String COUNTRY_NOT_ISO = "contract.errors.countryNotISO";
    public static final String LANGUAGE_NOT_ISO = "contract.errors.languageNotISO";

}
