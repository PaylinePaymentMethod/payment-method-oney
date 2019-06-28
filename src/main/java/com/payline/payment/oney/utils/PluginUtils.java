package com.payline.payment.oney.utils;


import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.exception.InvalidFieldFormatException;
import com.payline.payment.oney.exception.InvalidRequestException;
import com.payline.payment.oney.service.impl.RequestConfigServiceImpl;
import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.properties.constants.ConfigurationConstants.*;

public class PluginUtils {

    public static final String LINE_1 = "line1";
    public static final String LINE_4 = "line4";

    private PluginUtils() {
        // ras.
    }

    public static boolean isEmpty(String s) {

        return s == null || s.isEmpty();
    }

    public static <T> T requireNonNull(T obj, String message) throws InvalidRequestException {
        if (obj == null) {
            throw new InvalidRequestException(message);
        }
        return obj;
    }

    public static <T> T requireNonNull(Map map, String key, String err) throws InvalidRequestException {
        return PluginUtils.requireNonNull((T) map.get(key), err);
    }

// ------------  Methodes de mapping entre Oney et Payline  -----------------------


    // Mapping methods between  Payline and Oney
    //

    /**
     * Mapping Payline Buyer.legalStatus vers Oney personType
     * <p>
     * PAYLAPMEXT-147: personType must not be something else than 1 or 2.
     * The default value is 2.
     *
     * @param legalStatus Payline legalStatus
     * @return Oney personType
     */
    public static Integer getPersonType(Buyer.LegalStatus legalStatus) {
        switch (legalStatus) {
            case COMPANY:
                return 1;
            case PERSON:
                return 2;
            case UNKNOWN:
            default:
                return 2;
        }
    }

    /**
     * Mapping Payline Civility vers Oney HonorificCode
     *
     * @param civility Payline Civility
     * @return Oney HonorificCode
     */
    public static Integer getHonorificCode(String civility) {

        if (civility == null) {
            return null;
        }

        switch (civility.toLowerCase()) {

            // Madame → PAYLINE: 1 → ONEY: 2
            case "1":
                return 2;
            // Mademoiselle → PAYLINE: 3 → ONEY: 3
            case "3":
                return 3;
            // Monsieur → PAYLINE: 4 → ONEY: 1
            case "4":
                return 1;

            default:
                //Inconnu ex : Professeur, Maitre, Docteur
                return 0;
        }
    }

    public static Integer getOneyDeliveryModeCode(String paylineCode) {

        if (paylineCode == null) {
            return null;
        }
        switch (paylineCode) {

            // "Collection of the goods in the merchant store":
            case "1":
                return 1;
            // "Collection in a third party point":
            case "2":
                return 2;
            // case "Collection in an airport, train station or travel agency":
            case "3":
                return 3;
            //  case "Carrier (La Poste, Colissimo, UPS, DHL...or any private carrier)":
            case "4":
                return 4;
            //  case "Electronic ticket":
            case "5":
                return 5;

            default:
                return 4;

        }
    }

    public static Integer getOneyDeliveryOption(String paylineCode) {

        if (paylineCode == null) {
            return null;
        }
        switch (paylineCode.toLowerCase()) {
            //  cas "Express'
            case "1":
                return 1;
            //standard
            case "2":
                return 2;
            //non gere par Payline a ce jour
//            case "priority":
//                return 3;
            default:
                return 2;

        }
    }

    /**
     * Concatenates and trims two pieces of text, joining them by a space.
     *
     * @param text1 the first string to concatenate.
     * @param text2 the second string to concatenate.
     * @return The resulting string.
     */
    public static String spaceConcat(String text1, String text2) {
        StringBuffer sb = new StringBuffer();


        if (text1 != null) {
            sb.append(text1.trim());
        }
        if (text2 != null) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(text2.trim());
        }

        return sb.toString();
    }

    /**
     * Splits the given string into chunks of the given size, without truncating any word if possible.
     * If the given string does not contain
     *
     * @param toSplit   The long string to split.
     * @param maxLength The maximum length of a final text chunk.
     * @return A map containing as many lines as necessary
     * (key = "lineX" where X is the number of the line).
     */
    public static List<String> splitLongText(String toSplit, int maxLength) {
        List<String> chunks = new ArrayList<>();
        StringBuffer sb = new StringBuffer(toSplit);

        while (sb.length() > 0) {
            // remove whitespaces at the beginning
            if (Character.isWhitespace(sb.charAt(0))) {
                sb.delete(0, 1);
                continue;
            }

            // identify the next chunk
            String chunk;
            if (sb.length() <= maxLength) {
                chunk = sb.toString().trim();
            } else {
                int splitSpace = sb.substring(0, maxLength + 1).lastIndexOf(" ");
                int end = splitSpace >= 0 ? splitSpace : maxLength;
                chunk = sb.substring(0, end).trim();
            }

            chunks.add(chunk);

            // remove processed chunk
            sb.delete(0, chunk.length());
        }

        return chunks;
    }

// --------------------------- FIN methode de mapping -----------------------

    /**
     * Genere un merchant request id qui doit etre unique pour chaque requete
     *
     * @param merchantId
     * @return
     */
    public static String generateMerchantRequestId(String merchantId) {
        return merchantId + Calendar.getInstance().getTimeInMillis();
    }

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);


    /**
     * Return a ISO-3166 alpha 3 code  from a ISO-3166 alpha 2 code
     *
     * @param code
     * @return
     */
    public static String getIsoAlpha3CodeFromCountryCode2(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }
        Locale locale = new Locale("", code);
        return locale.getISO3Country();
    }

    /**
     * Return a country name from a ISO-3166 alpha 2 code
     *
     * @param code
     * @return
     */
    public static String getCountryNameCodeFromCountryCode2(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        Locale locale = new Locale("", code);
        return locale.getDisplayCountry();
    }

    public static String parseReference(String reference) throws InvalidFieldFormatException {

        if (reference == null || reference.isEmpty() || !reference.contains(OneyConstants.PIPE)) {
            throw new InvalidFieldFormatException("Oney reference should contain a '|' : " + reference, "Oney.PurchaseReference");
        }
        return reference.split(OneyConstants.PIPE)[1];
    }

    /**
     * check if a String respect ISO-3166 rules
     *
     * @param countryCode the code to compare
     * @return true if countryCode is in ISO-3166 list, else return false
     */
    public static boolean isISO3166(String countryCode) {
        return countryCode != null && Arrays.asList(Locale.getISOCountries()).contains(countryCode);
    }

    /**
     * check if a String respect ISO-3166 rules
     *
     * @param languageCode the code to compare
     * @return true if languageCode is in ISO-3166 list, else return false
     */
    public static boolean isISO639(String languageCode) {
        return languageCode != null && Arrays.asList(Locale.getISOLanguages()).contains(languageCode);
    }

    /**
     * Return a string which was converted from cents to euro
     *
     * @param amount
     * @return Amount as String
     */
    public static String createStringAmount(BigInteger amount, Currency currency) {
        //récupérer le nombre de digits dans currency
        int nbDigits = currency.getDefaultFractionDigits();

        StringBuilder sb = new StringBuilder();
        sb.append(amount);

        for (int i = sb.length(); i < 3; i++) {
            sb.insert(0, "0");
        }

        sb.insert(sb.length() - nbDigits, ".");
        return sb.toString();
    }

    /**
     * Return a Float which was converted from cents to euro
     *
     * @param amount   BigInteger
     * @param currency Currency
     * @return Amount as Float
     */
    public static Float createFloatAmount(BigInteger amount, Currency currency) {
        if (amount == null || currency == null) {
            return null;
        }
        return Float.parseFloat(createStringAmount(amount, currency));
    }

    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param refundRequest Payline RefundRequest
     * @return the ParametersMap
     */
    public static Map<String, String> getParametersMap(RefundRequest refundRequest) throws InvalidDataException {

        String authorization = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, PARTNER_AUTHORIZATION_KEY);
        String url = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, PARTNER_API_URL);
        String coutryCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(refundRequest, COUNTRY_CODE_KEY);
        return getParametersMap(authorization, url, coutryCode);
    }

    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param contractParametersCheckRequest Payline ContractParametersCheckRequest
     * @return the ParametersMap
     */
    public static Map<String, String> getParametersMap(ContractParametersCheckRequest contractParametersCheckRequest) throws InvalidDataException {

        String authorization = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, PARTNER_AUTHORIZATION_KEY);
        String url = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, PARTNER_API_URL);
        String coutryCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, COUNTRY_CODE_KEY);
        return getParametersMap(authorization, url, coutryCode);
    }

    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param transactionStatusRequest Payline TransactionStatusRequest
     * @return the ParametersMap
     */
    public static Map<String, String> getParametersMap(TransactionStatusRequest transactionStatusRequest) throws InvalidDataException {

        String authorization = RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, PARTNER_AUTHORIZATION_KEY);
        String url = RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, PARTNER_API_URL);
        String coutryCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(transactionStatusRequest, COUNTRY_CODE_KEY);
        return getParametersMap(authorization, url, coutryCode);
    }

    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param paymentRequest Payline PaymentRequest
     * @return the ParametersMap
     */
    public static Map<String, String> getParametersMap(PaymentRequest paymentRequest) throws InvalidDataException {

        String authorization = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PARTNER_AUTHORIZATION_KEY);
        String url = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, PARTNER_API_URL);
        String coutryCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(paymentRequest, COUNTRY_CODE_KEY);
        return getParametersMap(authorization, url, coutryCode);
    }


    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param authorization PARTNER_AUTHORIZATION_KEY
     * @param url           PARTNER_API_URL
     * @param coutryCode    coutryCode from ContractParameters
     * @return the ParametersMap
     */
    private static Map<String, String> getParametersMap(String authorization, String url, String coutryCode) throws InvalidDataException {

        if (coutryCode == null || coutryCode.isEmpty()) {
            throw new InvalidDataException("coutryCode is mandatory", "coutryCode");
        }

        if (authorization == null) {
            throw new InvalidDataException(PARTNER_AUTHORIZATION_KEY + " is mandatory", PARTNER_AUTHORIZATION_KEY);
        }

        if (url == null) {
            throw new InvalidDataException(PARTNER_API_URL + " is mandatory", PARTNER_API_URL);
        }


        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(PARTNER_AUTHORIZATION_KEY, authorization);
        parametersMap.put(PARTNER_API_URL, url);
        parametersMap.put(HEADER_COUNTRY_CODE, coutryCode.toUpperCase());
        if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            parametersMap.put(SECRET_KEY, SECRET_VALUE_ON);
        } else {
            parametersMap.put(SECRET_KEY, SECRET_VALUE_OFF);
        }

        return parametersMap;
    }

    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param captureRequest Payline captureRequest
     * @return the ParametersMap
     */
    public static Map<String, String> getParametersMap(CaptureRequest captureRequest) throws InvalidDataException {

        String authorization = RequestConfigServiceImpl.INSTANCE.getParameterValue(captureRequest, PARTNER_AUTHORIZATION_KEY);
        String url = RequestConfigServiceImpl.INSTANCE.getParameterValue(captureRequest, PARTNER_API_URL);
        String coutryCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(captureRequest, COUNTRY_CODE_KEY);
        return getParametersMap(authorization, url, coutryCode);
    }


    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param notificationRequest Payline NotificationRequest
     * @return the ParametersMap
     */
    public static Map<String, String> getParametersMap(NotificationRequest notificationRequest) throws InvalidDataException {

        String authorization = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, PARTNER_AUTHORIZATION_KEY);
        String url = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, PARTNER_API_URL);
        String coutryCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(notificationRequest, COUNTRY_CODE_KEY);
        return getParametersMap(authorization, url, coutryCode);
    }


    public static String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }

    public static String createMerchantContext(PaymentRequest paymentRequest) {
        return (paymentRequest.isCaptureNow() ? CAPTURE_NOW : CAPTURE_LATER)
                + SEPARATOR
                + paymentRequest.getAmount().getAmountInSmallestUnit() // adding amount in merchant_context
                + SEPARATOR
                + paymentRequest.getAmount().getCurrency().getCurrencyCode(); // addingCurrency in merchant_context
    }

    public static boolean isCaptureNow(String s) {
        return (CAPTURE_NOW.equals(s.split(SEPARATOR)[0]));
    }

    public static float getAmount(String s) {
        String a = s.split(SEPARATOR)[1];
        String c = s.split(SEPARATOR)[2];

        int cl = Currency.getInstance(c).getDefaultFractionDigits();

        String finalAmount = a.substring(0, a.length() - cl)
                + "."
                + a.substring(a.length() - cl);
        return Float.valueOf(finalAmount);
    }

    public static String getCurrency(String s) {
        return s.split(SEPARATOR)[2];
    }
}