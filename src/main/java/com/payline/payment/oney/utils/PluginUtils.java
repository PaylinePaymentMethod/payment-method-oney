package com.payline.payment.oney.utils;


import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.exception.InvalidRequestException;
import com.payline.payment.oney.utils.config.ConfigEnvironment;
import com.payline.pmapi.bean.ActionRequest;
import com.payline.pmapi.bean.Request;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Pattern;

import static com.payline.payment.oney.utils.OneyConstants.*;

public class PluginUtils {


    public static final String URL_DELIMITER = "/";
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

    public static ConfigEnvironment getEnvironnement(ActionRequest actionRequest) {
        return actionRequest.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
    }

    public static ConfigEnvironment getEnvironnement(Request request) {
        return request.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
    }

    public static ConfigEnvironment getEnvironnement(ContractParametersCheckRequest contractParametersCheckRequest) {
        return contractParametersCheckRequest.getEnvironment().isSandbox() ? ConfigEnvironment.DEV : ConfigEnvironment.PROD;
    }

// ------------  Methodes de mapping entre Oney et Payline  -----------------------


    //Mapping methods betwen  Payline and Oney
    public static Integer getPersonType(Buyer.LegalStatus legalStatus) {
        switch (legalStatus) {
            case UNKNOWN:
                return 0;
            case COMPANY:
                return 1;
            case PERSON:
                return 2;
        }
        return null;
    }

    public static Integer getHonorificCode(String civility) {

        if (civility == null) {
            return null;
        }
        switch (civility.toLowerCase()) {
            //Inconnu ex : professeu, Maitre, docteur
            case "7":
            case "9":
            case "10":
            case "11":
                return 0;
            //MR
            case "4":
            case "5":
                return 1;
            //MME
            case "1":
            case "2":
            case "6":
                return 2;
            //MLLE
            case "3":
                return 3;
            default:
                return 1;
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
     * Decoupe le texte en 5 renvoi un tableau
     *
     * @param longText
     * @param longText2
     * @param size
     * @return
     */
    public static Map<String, String> truncateLongText(String longText, String longText2, int size) {
        Map<String, String> textTruncated = new HashMap();

        String line1;
        String line2;
        String line3;
        String line4;
        String line5;

        int fromIndex = size;
        int fromIndex2 = size;
        int firstCharPosition = 0;
        int firstCharPosition2 = 0;

        if (longText == null) {
            textTruncated.put(LINE_1, "");
        }
        //-------------------- address 1
        else if (longText.length() < size) {
            textTruncated.put(LINE_1, longText);

        } else {
            int end1 = longText.lastIndexOf(' ', fromIndex);
            line1 = longText.substring(firstCharPosition, end1);
            fromIndex += line1.length();
            firstCharPosition += line1.length();
            textTruncated.put(LINE_1, line1);

            int end2 = longText.lastIndexOf(' ', fromIndex);
            line2 = longText.substring(firstCharPosition, end2);
            fromIndex += line2.length();
            firstCharPosition += line2.length();
            textTruncated.put("line2", line2);

            int end3 = longText.lastIndexOf(' ', fromIndex);
            line3 = longText.substring(firstCharPosition, end3);
            textTruncated.put("line3", line3);
        }
        //-------------------- address 2
        if (longText2 == null) {
            textTruncated.put(LINE_4, "");

        } else if (longText2.length() < size) {
            textTruncated.put(LINE_4, longText2);

        } else {
            int end4 = longText2.lastIndexOf(' ', fromIndex2);
            line4 = longText2.substring(firstCharPosition2, end4);
            fromIndex2 += line4.length();
            firstCharPosition2 += line4.length();
            textTruncated.put(LINE_4, line4);

            int end5 = longText2.lastIndexOf(' ', fromIndex2);
            line5 = longText2.substring(firstCharPosition2, end5);
            textTruncated.put("line5", line5);
        }
        return textTruncated;

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
        Locale locale = new Locale("", code);
        return locale.getDisplayCountry();
    }

    public static String generateReference(Purchase purchase) {

        return purchase.getExternalReferenceType() + OneyConstants.PIPE + purchase.getExternalReference();
    }

    public static String parseReference(String reference) throws InvalidRequestException {

        if (reference == null || reference.isEmpty() || !reference.contains(OneyConstants.PIPE)) {
            throw new InvalidRequestException("Oney reference should contain a '|' : " + reference);
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
        return Arrays.asList(Locale.getISOCountries()).contains(countryCode);
    }

    /**
     * check if a String respect ISO-3166 rules
     *
     * @param languageCode the code to compare
     * @return true if languageCode is in ISO-3166 list, else return false
     */
    public static boolean isISO639(String languageCode) {
        return Arrays.asList(Locale.getISOLanguages()).contains(languageCode);
    }

    /**
     * Return a string which was converted from cents to euro
     *
     * @param amount
     * @return
     */
    public static String createStringAmount(BigInteger amount,Currency currency) {
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
     * @param amount
     * @return
     */
    public static Float createFloatAmount(BigInteger amount, Currency currency) {
        return Float.parseFloat(createStringAmount(amount,currency));
    }


    public static boolean getRefundFlag(String transactionStatusRequest) {
        switch (transactionStatusRequest) {
            case "FUNDED":
                return true;

            case "PENDING":
            case "FAVORABLE":
                return false;

            case "REFUSED":
            case "ABORTED":
            case "CANCELLED":
                throw new IllegalStateException("a " + transactionStatusRequest + " transactionStatusRequest can't be cancelled");

            default:
                throw new IllegalStateException(transactionStatusRequest + " is not a valid status for refund or cancel");

        }

    }

    /**
     * Buid a map with all needed parameters for HTTP calls
     *
     * @param partnerConfiguration Payline PartnerConfiguration
     * @param coutryCode           coutryCode from ContractParameters
     * @return
     */
    public static Map<String, String> getParametersMap(PartnerConfiguration partnerConfiguration, String coutryCode) {

        if (coutryCode == null || coutryCode.isEmpty()) {
            throw new IllegalStateException("coutryCode is mandatory");
        }

        String authorization = partnerConfiguration.getProperty(PARTNER_AUTHRIZATION_KEY);
        if (authorization == null) {
            throw new IllegalStateException(PARTNER_AUTHRIZATION_KEY + " is mandatory");
        }

        String url = partnerConfiguration.getProperty(PARTNER_API_URL);
        if (url == null) {
            throw new IllegalStateException(PARTNER_API_URL + " is mandatory");
        }


        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(PARTNER_AUTHRIZATION_KEY, authorization);
        parametersMap.put(PARTNER_API_URL, url);
        parametersMap.put(HEADER_COUNTRY_CODE, coutryCode.toUpperCase());

        return parametersMap;
    }
}