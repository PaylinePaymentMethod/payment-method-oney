package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.OneyError40x;
import com.payline.payment.oney.bean.request.OneyEncryptedRequest;
import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.utils.OneyCheckConstants;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.payment.oney.utils.i18n.I18nService;
import com.payline.payment.oney.utils.properties.constants.ConfigurationConstants;
import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import com.payline.payment.oney.utils.properties.service.ReleasePropertiesEnum;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.PasswordParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest.GENERIC_ERROR;

public class ConfigurationServiceImpl implements ConfigurationService {

    private static final Logger LOGGER = LogManager.getLogger(ConfigurationServiceImpl.class);

    private I18nService i18n = I18nService.getInstance();

    private OneyHttpClient httpClient = OneyHttpClient.getInstance();

    @Override
    public List<AbstractParameter> getParameters(Locale locale) {
        List<AbstractParameter> parameters = new ArrayList<>();

        // merchant GUID
        final InputParameter merchantGuid = new InputParameter();
        merchantGuid.setKey(MERCHANT_GUID_KEY);
        merchantGuid.setLabel(this.i18n.getMessage(MERCHANT_GUID_LABEL, locale));
        merchantGuid.setDescription(this.i18n.getMessage(MERCHANT_GUID_DESCRIPTION, locale));
        merchantGuid.setRequired(true);
        parameters.add(merchantGuid);

        // OPC
        final InputParameter opc = new InputParameter();
        opc.setKey(OPC_KEY);
        opc.setLabel(this.i18n.getMessage(OPC_LABEL, locale));
        opc.setDescription(this.i18n.getMessage(OPC_DESCRIPTION, locale));
        opc.setRequired(true);
        parameters.add(opc);

        // NB Echeances
        final ListBoxParameter nbEcheancesParameter = new ListBoxParameter();
        nbEcheancesParameter.setKey(NB_ECHEANCES_KEY);
        nbEcheancesParameter.setLabel(this.i18n.getMessage(NB_ECHEANCES_LABEL, locale));
        nbEcheancesParameter.setRequired(true);
        final LinkedHashMap<String, String> nbEcheances = new LinkedHashMap<>();
        nbEcheances.put("3x", "3x");
        nbEcheances.put("4x", "4x");
        nbEcheances.put("6x", "6x");
        nbEcheances.put("10x", "10x");
        nbEcheances.put("12x", "12x");
        nbEcheancesParameter.setList(nbEcheances);
        parameters.add(nbEcheancesParameter);


        // X-Oney-Partner-Country-Code ISO 3166-1 alpha-2
        final ListBoxParameter codePays = new ListBoxParameter();
        codePays.setKey(COUNTRY_CODE_KEY);
        codePays.setLabel(this.i18n.getMessage(COUNTRY_CODE_LABEL, locale));
        codePays.setDescription(COUNTRY_CODE_DESCRIPTION);
        codePays.setRequired(true);
        final LinkedHashMap<String, String> codes = new LinkedHashMap<>();
        codes.put("FR", "FR");
        codes.put("BE", "BE");
        codes.put("IT", "IT");
        codes.put("ES", "ES");
        codePays.setList(codes);
        codePays.setValue("FR");
        parameters.add(codePays);

        //merchant_language_code ISO 639-1
        final InputParameter merchantLanguageCode = new InputParameter();
        merchantLanguageCode.setKey(LANGUAGE_CODE_KEY);
        merchantLanguageCode.setLabel(this.i18n.getMessage(LANGUAGE_CODE_LABEL, locale));
        merchantLanguageCode.setDescription(this.i18n.getMessage(LANGUAGE_CODE_DESCRIPTION, locale));
        merchantLanguageCode.setRequired(false);
        parameters.add(merchantLanguageCode);

        //X-Oney-International-Partner-ID
        //Optional Unique international code for the partner (merchant)
        final InputParameter internationalPartnerId = new InputParameter();
        internationalPartnerId.setKey(ID_INTERNATIONAL_KEY);
        internationalPartnerId.setLabel(this.i18n.getMessage(ID_INTERNATIONAL_LABEL, locale));
        internationalPartnerId.setDescription(ID_INTERNATIONAL_DESCRIPTION);
        internationalPartnerId.setRequired(false);
        parameters.add(internationalPartnerId);


        // secret key
        final PasswordParameter secretKey = new PasswordParameter();
        secretKey.setKey(PARTNER_CHIFFREMENT_KEY);
        secretKey.setLabel(this.i18n.getMessage(PARTNER_CHIFFREMENT_LABEL, locale));
        secretKey.setDescription(this.i18n.getMessage(PARTNER_CHIFFREMENT_DESCRIPTION, locale));
        secretKey.setRequired(false);
        parameters.add(secretKey);

        return parameters;

    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {

        Locale locale = contractParametersCheckRequest.getLocale();
        final Map<String, String> errors = new HashMap<>();

        // psp id
        String pspId = null;
        String merchantGuid = null;
        String opcKey = null;
        String codePays = null;
        try {

            codePays = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, COUNTRY_CODE_KEY);
            if (codePays == null) {
                errors.put(COUNTRY_CODE_KEY, this.i18n.getMessage(COUNTRY_CODE_MESSAGE_ERROR, locale));
                return errors;
            } else if (!PluginUtils.isISO3166(codePays)) {
                errors.put(COUNTRY_CODE_KEY, this.i18n.getMessage(COUNTRY_NOT_ISO, locale));
                return errors;
            }

        } catch (InvalidDataException e) {
            errors.put(e.getErrorCodeOrLabel(), this.i18n.getMessage(COUNTRY_CODE_MESSAGE_ERROR, locale));
        }

        try {

            pspId = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, PSP_GUID_KEY);
            if (PluginUtils.isEmpty(pspId)) {
                errors.put(PSP_GUID_KEY, this.i18n.getMessage(PSP_GUID_MESSAGE_ERROR, locale));
            }

        } catch (InvalidDataException e) {
            errors.put(e.getErrorCodeOrLabel(), this.i18n.getMessage(PSP_GUID_MESSAGE_ERROR, locale));
        }

        try {
            // apiKey
            final String partnerKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, PARTNER_AUTHORIZATION_KEY);
            if (PluginUtils.isEmpty(partnerKey)) {
                errors.put(PARTNER_AUTHORIZATION_KEY, this.i18n.getMessage(PARTNER_AUTHORIZATION_KEY_MESSAGE_ERROR, locale));
            }
        } catch (InvalidDataException e) {
            errors.put(e.getErrorCodeOrLabel(), this.i18n.getMessage(PARTNER_AUTHORIZATION_KEY_MESSAGE_ERROR, locale));
        }


        try {
            // merchant guid
            merchantGuid = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, MERCHANT_GUID_KEY);
            if (PluginUtils.isEmpty(merchantGuid)) {
                errors.put(MERCHANT_GUID_KEY, this.i18n.getMessage(MERCHANT_GUID_MESSAGE_ERROR, locale));
            }
        } catch (InvalidDataException e) {
            errors.put(e.getErrorCodeOrLabel(), this.i18n.getMessage(MERCHANT_GUID_MESSAGE_ERROR, locale));
        }

        try {
            // OPC
            opcKey = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, OPC_KEY);
            if (PluginUtils.isEmpty(opcKey)) {
                errors.put(OPC_KEY, this.i18n.getMessage(OPC_MESSAGE_ERROR, locale));
            }
        } catch (InvalidDataException e) {
            errors.put(e.getErrorCodeOrLabel(), this.i18n.getMessage(OPC_MESSAGE_ERROR, locale));
        }


        try {
            // language code n'est pas obligatoire
            final String merchantLanguageCode = RequestConfigServiceImpl.INSTANCE.getParameterValue(contractParametersCheckRequest, LANGUAGE_CODE_KEY);
            if (merchantLanguageCode != null && !PluginUtils.isISO639(merchantLanguageCode)) {
                errors.put(LANGUAGE_CODE_KEY, this.i18n.getMessage(LANGUAGE_NOT_ISO, locale));
            }
        } catch (InvalidDataException e) {
            errors.put(e.getErrorCodeOrLabel(), this.i18n.getMessage(LANGUAGE_NOT_ISO, locale));
        }


        if (!errors.isEmpty()) {
            return errors;
        }

        return validateCall(contractParametersCheckRequest, errors, pspId, merchantGuid, opcKey, codePays, contractParametersCheckRequest.getEnvironment().isSandbox());
    }

    private Map<String, String> validateCall(ContractParametersCheckRequest contractParametersCheckRequest, Map<String, String> errors, String pspId, String merchantGuid, String opcKey, String codePays, boolean isSandbox) {
        try {

            String jsonMsg = getFinalJsonMessage(pspId, merchantGuid, opcKey, codePays);
            Map<String, String> parameters = PluginUtils.getParametersMap(contractParametersCheckRequest);
            StringResponse stringResponse;
            if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
                OneyEncryptedRequest requestEncrypted = OneyEncryptedRequest.fromJson(jsonMsg, contractParametersCheckRequest);
                stringResponse = httpClient.initiateCheckPayment(requestEncrypted.toString(), parameters, isSandbox);
            } else {
                stringResponse = httpClient.initiateCheckPayment(jsonMsg, parameters, isSandbox);
            }
            if (stringResponse == null) {
                errors.put(PARTNER_API_URL, UNEXPECTED_ERR);
                LOGGER.error("HTTP response is not parsable");
            } else {
                OneyError40x err = null;

                switch (stringResponse.getCode()) {
                    case HTTP_404:
                        errors.put(PARTNER_API_URL, "HTTP CODE 404");
                        break;
                    case HTTP_401:
                        err = OneyError40x.parseJson(stringResponse.getContent());
                        String errMsg = err.getPrintableMessage();
                        LOGGER.error(errMsg);
                        // it could be the authorisation key
                        if (errMsg.contains("invalid subscription key")) {
                            errors.put(PARTNER_AUTHORIZATION_KEY, err.getMessage());
                        } else {
                            // if not,  it's psp_guid or merchant_guid. we assume psp_guid is correct
                            errors.put(MERCHANT_GUID_KEY, err.getMessage());

                        }
                        break;
                    case HTTP_400:
                        err = OneyError40x.parseJson(stringResponse.getContent());
                        if (err.getMessage().contains("X-Oney-Partner-Country-Code")) {
                            LOGGER.error("Paramètre {} incorrect (non supporté par Oney) {}", COUNTRY_CODE_KEY, err.getMessage());
                            errors.put(COUNTRY_CODE_KEY, err.getMessage());
                        } else {
                            errMsg = stringResponse.toString();
                            checkOpcError(errors, errMsg);
                        }
                        break;
                    case HTTP_409:
                        err = OneyError40x.parseJson(stringResponse.getContent());
                        errors.put(GENERIC_ERROR, err.getMessage() );
                        break;
                    case HTTP_500:
                        errMsg = stringResponse.toString();
                        checkOpcError(errors, errMsg);
                        break;
                    default:
                        break;
                }

            }

        } catch (DecryptException e) {
            LOGGER.error("URL call throws an DecryptException", e);
            errors.put(PARTNER_CHIFFREMENT_KEY, e.getMessage());
        } catch (HttpCallException e) {
            LOGGER.error("URL call throws an HttpCallException", e);
            errors.put(PARTNER_API_URL, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("HTTP response is not parsable");
            errors.put(PARTNER_API_URL, UNEXPECTED_ERR);
        }
        return errors;
    }

    private void checkOpcError(Map<String, String> errors, String errMsg) {

        if (errMsg != null && errMsg.toLowerCase().contains("business") && errMsg.toLowerCase().contains("transaction")) {
            LOGGER.error(errMsg);
            errors.put(OPC_KEY, OPC_MESSAGE_ERROR);
        }
    }

    private String getFinalJsonMessage(String pspId, String merchantGuid, String opcKey, String codePays) {
        String lang = "";
        Locale[] all = Locale.getAvailableLocales();
        for (Locale locale : all) {
            String country = locale.getCountry();
            if (country.equalsIgnoreCase(codePays)) {
                lang = locale.getLanguage();
                break;
            }
        }
        return OneyCheckConstants.TEST_JSON_MSG.replace(OneyCheckConstants.PSP_GUID_TAG, pspId)
                .replace(OneyCheckConstants.MERCHANT_GUID_TAG, merchantGuid)
                .replace(OneyCheckConstants.OPC_KEY_TAG, opcKey)
                .replace(OneyCheckConstants.COUNTRY_ADDRESS, (new Locale("", codePays)).getISO3Country())
                .replace(OneyCheckConstants.LANGUAGE_CODE, lang);
    }

    @Override
    public ReleaseInformation getReleaseInformation() {

        LocalDate date = LocalDate.parse(ReleasePropertiesEnum.INSTANCE.get(ConfigurationConstants.RELEASE_DATE),
                DateTimeFormatter.ofPattern(ConfigurationConstants.RELEASE_DATE_FORMAT));
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(date)
                .withVersion(ReleasePropertiesEnum.INSTANCE.get(ConfigurationConstants.RELEASE_VERSION))
                .build();
    }

    @Override
    public String getName(Locale locale) {

        return this.i18n.getMessage(ConfigurationConstants.PAYMENT_METHOD_NAME, locale);
    }
}
