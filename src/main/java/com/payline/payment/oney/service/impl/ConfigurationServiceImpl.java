package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.i18n.I18nService;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.NetworkListBoxParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.PasswordParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.payment.oney.utils.OneyConstants.*;

public class ConfigurationServiceImpl implements ConfigurationService {


    private static final Logger LOGGER = LogManager.getLogger(ConfigurationServiceImpl.class);

    private I18nService i18n = I18nService.getInstance();

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

        //Api Key
        final PasswordParameter apiKey = new PasswordParameter();
        apiKey.setKey(X_ONEY_AUTHORIZATION_KEY);
        apiKey.setLabel(this.i18n.getMessage(X_ONEY_AUTHORIZATION_LABEL, locale));
        apiKey.setDescription(this.i18n.getMessage(X_ONEY_AUTHORIZATION_DESCRIPTION, locale));
        apiKey.setRequired(true);
        parameters.add(apiKey);

        // psp guid
        final InputParameter pspGuid = new InputParameter();
        pspGuid.setKey(PSP_GUID_KEY);
        pspGuid.setLabel(this.i18n.getMessage(PSP_GUID_LABEL, locale));
        pspGuid.setDescription(this.i18n.getMessage(PSP_GUID_DESCRIPTION, locale));
        pspGuid.setRequired(true);
        parameters.add(pspGuid);

        // api Marketing
        final PasswordParameter apiMarketing = new PasswordParameter();
        apiMarketing.setKey(API_MARKETING_KEY);
        apiMarketing.setLabel(this.i18n.getMessage(API_MARKETING_LABEL, locale));
        apiMarketing.setDescription(this.i18n.getMessage(API_MARKETING_DESCRIPTION, locale));
        apiMarketing.setRequired(true);
        parameters.add(apiMarketing);

        // OPC
        final InputParameter opc = new InputParameter();
        opc.setKey(API_MARKETING_KEY);
        opc.setLabel(this.i18n.getMessage(API_MARKETING_LABEL, locale));
        opc.setDescription(this.i18n.getMessage(API_MARKETING_DESCRIPTION, locale));
        opc.setRequired(true);
        parameters.add(opc);

        // NB Echeances
        final NetworkListBoxParameter nbEcheances = new NetworkListBoxParameter();
        nbEcheances.setKey(NB_ECHEANCES_KEY);
        nbEcheances.setLabel(this.i18n.getMessage(NB_ECHEANCES_LABEL, locale));
        nbEcheances.setRequired(true);
        parameters.add(nbEcheances);

        // X-Oney-Partner-Country-Code
        final NetworkListBoxParameter codePays = new NetworkListBoxParameter();
        codePays.setKey(COUNTRY_CODE_KEY);
        codePays.setLabel(this.i18n.getMessage(COUNTRY_CODE_LABEL, locale));
        codePays.setDescription(COUNTRY_CODE_DESCRIPTION);
        codePays.setRequired(false);
        parameters.add(codePays);

        //merchant_language_code
        final NetworkListBoxParameter merchantLanguageCode = new NetworkListBoxParameter();
        merchantLanguageCode.setKey(LANGUAGE_CODE_KEY);
        merchantLanguageCode.setLabel(this.i18n.getMessage(LANGUAGE_CODE_LABEL, locale));
        merchantLanguageCode.setDescription(this.i18n.getMessage(LANGUAGE_CODE_DESCRIPTION, locale));
        merchantLanguageCode.setRequired(false);
        parameters.add(merchantLanguageCode);

        //X-Oney-International-Partner-ID
        final InputParameter internationalPartnerId = new InputParameter();
        internationalPartnerId.setKey(ID_INTERNATIONAL_KEY);
        internationalPartnerId.setLabel(this.i18n.getMessage(ID_INTERNATIONAL_LABEL, locale));
        internationalPartnerId.setDescription(ID_INTERNATIONAL_DESCRIPTION);
        internationalPartnerId.setRequired(false);
        parameters.add(internationalPartnerId);


        return parameters;

    }

    @Override
    public Map<String, String> check(ContractParametersCheckRequest contractParametersCheckRequest) {

        Locale locale = contractParametersCheckRequest.getLocale();
        final Map<String, String> accountInfo = contractParametersCheckRequest.getAccountInfo();
        final Map<String, String> errors = new HashMap<>();

        // psp id
        final String apiKey = accountInfo.get(X_ONEY_AUTHORIZATION_KEY);
        if (PluginUtils.isEmpty(apiKey)) {
            errors.put(X_ONEY_AUTHORIZATION_KEY, this.i18n.getMessage(X_ONEY_AUTHORIZATION_MESSAGE_ERROR, locale));
        }

        // psp id
        final String pspId = accountInfo.get(PSP_GUID_KEY);
        if (PluginUtils.isEmpty(pspId)) {
            errors.put(PSP_GUID_KEY, this.i18n.getMessage(PSP_GUID_MESSAGE_ERROR, locale));
        }

        // merchant guid
        final String merchantGuid = accountInfo.get(MERCHANT_GUID_KEY);
        if (PluginUtils.isEmpty(merchantGuid)) {
            errors.put(MERCHANT_GUID_KEY, this.i18n.getMessage(MERCHANT_GUID_MESSAGE_ERROR, locale));
        }
        return errors;
    }

    @Override
    public ReleaseInformation getReleaseInformation() {
        Properties props = new Properties();
        try {
            props.load(ConfigurationServiceImpl.class.getClassLoader().getResourceAsStream("release.properties"));
        } catch (IOException e) {
            final String message = "An error occurred reading the file: release.properties";
            LOGGER.error(message);
            throw new RuntimeException(message, e);
        }

        LocalDate date = LocalDate.parse(props.getProperty("release.date"), DateTimeFormatter.ofPattern(RELEASE_DATE_FORMAT));
        return ReleaseInformation.ReleaseBuilder.aRelease()
                .withDate(date)
                .withVersion(props.getProperty(RELEASE_VERSION))
                .build();
    }

    @Override
    public String getName(Locale locale) {

        return this.i18n.getMessage(PAYMENT_METHOD_NAME, locale);
    }
}
