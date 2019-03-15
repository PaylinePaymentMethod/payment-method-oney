package com.payline.payment.oney.utils.http;


import com.payline.payment.oney.bean.request.*;
import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;

/**
 * Created by Thales on  27/11/2018
 */
public class OneyHttpClient extends AbstractHttpClient {

    public static final String PSP_GUID = "psp_guid";
    public static final String MERCHANT_GUID = "merchant_guid";
    public static final String REFERENCE = "reference";
    public static final String LANGUAGE_CODE = "language_code";
    public static final String PSP_GUID_TAG = "/psp_guid/";
    public static final String MERCHANT_GUID_TAG = "/merchant_guid/";
    public static final String REFERENCE_TAG = "/reference/";

    /**
     * Instantiate a HTTP client with default values.
     */
    private OneyHttpClient() {
        super();
    }

    /**
     * Singleton Holder
     */
    private static class SingletonHolder {
        private static final OneyHttpClient INSTANCE = new OneyHttpClient();
    }

    /**
     * @return the singleton instance
     */
    public static OneyHttpClient getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * Send a POST request, with a XML content type.
     *
     * @param path           URL path
     * @param requestContent The JSON content, as a string
     * @return The response returned from the HTTP call
     * @throws HttpCallException COMMUNICATION_ERROR
     */
    public StringResponse doPost(String path, String requestContent, Map<String, String> params)
            throws HttpCallException {

        String url = params.get(PARTNER_API_URL);
        StringEntity entity = new StringEntity(requestContent, StandardCharsets.UTF_8);
        Header[] headers = createHeaders(params);

        return super.doPost(url, path, headers, entity);
    }

    /**
     * Send a GET request, with a XML content type.
     *
     * @param path URL path
     * @return The response returned from the HTTP call
     * @throws HttpCallException COMMUNICATION_ERROR
     */
    public StringResponse doGet(String path, Map<String, String> params, Map<String, String> urlParameters)
            throws HttpCallException {

        String url = params.get(PARTNER_API_URL);

        //build Request
        String finalPath = this.buildGetOrderPath(path, params);

        // add url parameters
        if( urlParameters.size() > 0 && urlParameters.get(LANGUAGE_CODE) != null ){
            finalPath += "?" + LANGUAGE_CODE + "=" + urlParameters.get(LANGUAGE_CODE);
        }

        Header[] headers = createHeaders(params);


        return super.doGet(url, finalPath, headers);
    }

    public String buildGetOrderPath(String path, Map<String, String> param) {

        return path + PSP_GUID_TAG + param.get(PSP_GUID)
                + MERCHANT_GUID_TAG + param.get(MERCHANT_GUID)
                + REFERENCE_TAG + param.get(REFERENCE);

    }

    public String buildConfirmOrderPath(String path, Map<String, String> params) {

        return buildGetOrderPath(path, params) + "/action/confirm";
    }

    public String buildRefundOrderPath(String path, Map<String, String> params) {

        return buildGetOrderPath(path, params) + "/action/cancel";
    }

    /**
     * Create header for POST/GET methdod
     *
     * @return Header[]  header data
     */
    private Header[] createHeaders(Map<String, String> params) {

        String countryCode = params.get(HEADER_COUNTRY_CODE);
        String authorizationKey = params.get(PARTNER_AUTHORIZATION_KEY);
        String secretValue = params.get(SECRET_KEY);
        Header[] headers = new Header[4];
        headers[0] = new BasicHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        headers[1] = new BasicHeader(AUTHORIZATION, authorizationKey);
        headers[2] = new BasicHeader(COUNTRY_CODE_HEADER, countryCode);
        headers[3] = new BasicHeader(SECRET_KEY, secretValue);

        return headers;
    }

    public StringResponse initiatePayment(OneyPaymentRequest request)
            throws PluginTechnicalException {

        Map<String, String> parameters = new HashMap<>(request.getCallParameters());
        String jsonBody;
        if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            OneyEncryptedRequest requestEncrypted = OneyEncryptedRequest.fromOneyPaymentRequest(request);
            jsonBody = requestEncrypted.toString();
        } else {
            jsonBody = request.toString();
        }
        // do the request
        return doPost(PAYMENT_REQUEST_URL, jsonBody, parameters);

    }

    public StringResponse initiateCheckPayment(String jsonBody, Map<String, String> parameters)
            throws HttpCallException {
        // do the request
        return doPost(PAYMENT_REQUEST_URL, jsonBody, parameters);

    }

    public StringResponse initiateConfirmationPayment(OneyConfirmRequest request)
            throws PluginTechnicalException {
        Map<String, String> parameters = new HashMap<>(request.getCallParameters());
        parameters.put(PSP_GUID, request.getPspGuid());
        parameters.put(MERCHANT_GUID, request.getMerchantGuid());
        parameters.put(REFERENCE, request.getPurchaseReference());
        String path = buildConfirmOrderPath(CONFIRM_REQUEST_URL, parameters);
        String jsonBody = null;
        if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            OneyEncryptedRequest requestEncrypted = OneyEncryptedRequest.fromOneyConfirmRequest(request);
            jsonBody = requestEncrypted.toString();
        } else {
            jsonBody = request.toString();
        }
        // do the request
        return doPost(path, jsonBody, parameters);

    }

    public StringResponse initiateRefundPayment(OneyRefundRequest request)
            throws PluginTechnicalException {
        Map<String, String> parameters = new HashMap<>(request.getCallParameters());
        parameters.put(PSP_GUID, request.getPspGuid());
        parameters.put(MERCHANT_GUID, request.getMerchantGuid());
        parameters.put(REFERENCE, request.getPurchaseReference());
        String path = buildRefundOrderPath(CANCEL_REQUEST_URL, parameters);
        String jsonBody;
        if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            OneyEncryptedRequest requestEncrypted = OneyEncryptedRequest.fromOneyRefundRequest(request);
            jsonBody = requestEncrypted.toString();
        } else {
            jsonBody = request.toString();
        }
        // do the request
        return doPost(path, jsonBody, parameters);

    }

    public StringResponse initiateGetTransactionStatus(OneyTransactionStatusRequest request)
            throws HttpCallException {
        Map<String, String> parameters = new HashMap<>(request.getCallParameters());
        parameters.put(PSP_GUID, request.getPspGuid());
        parameters.put(MERCHANT_GUID, request.getMerchantGuid());
        parameters.put(REFERENCE, request.getPurchaseReference());

        Map<String, String> urlParameters = new HashMap<>();
        urlParameters.put(LANGUAGE_CODE, request.getLanguageCode());

        // do the request
        return doGet(STATUS_REQUEST_URL, parameters, urlParameters);

    }
}
