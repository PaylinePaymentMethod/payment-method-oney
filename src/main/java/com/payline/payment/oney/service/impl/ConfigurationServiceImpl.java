package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.utils.PluginUtils;
import com.payline.payment.oney.utils.i18n.I18nService;
import com.payline.payment.oney.utils.properties.constants.ConfigurationConstants;
import com.payline.payment.oney.utils.properties.service.ReleasePropertiesEnum;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.ListBoxParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.PasswordParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.service.ConfigurationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.payline.payment.oney.utils.OneyConstants.*;

public class ConfigurationServiceImpl implements ConfigurationService {


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
        nbEcheances.put("3", "3");
        nbEcheances.put("4", "4");
        nbEcheancesParameter.setList(nbEcheances);
        parameters.add(nbEcheancesParameter);

        // X-Oney-Partner-Country-Code ISO 3166-1 alpha-2
        final InputParameter codePays = new InputParameter();
        codePays.setKey(COUNTRY_CODE_KEY);
        codePays.setLabel(this.i18n.getMessage(COUNTRY_CODE_LABEL, locale));
        codePays.setDescription(COUNTRY_CODE_DESCRIPTION);
        codePays.setRequired(true);
        parameters.add(codePays);

        //merchant_language_code ISO 639-1
        final InputParameter merchantLanguageCode = new InputParameter();
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

        // apiKey
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

        String merchantLanguageCode = contractParametersCheckRequest.getContractConfiguration().getProperty(LANGUAGE_CODE_KEY).getValue();
        if (!PluginUtils.isISO639(merchantLanguageCode)) {
            errors.put(LANGUAGE_CODE_KEY, this.i18n.getMessage(LANGUAGE_NOT_ISO, locale));
        }

        String codePays = contractParametersCheckRequest.getContractConfiguration().getProperty(COUNTRY_CODE_DESCRIPTION).getValue();
        if (!PluginUtils.isISO3166(codePays)) {
            errors.put(COUNTRY_CODE_DESCRIPTION, this.i18n.getMessage(COUNTRY_NOT_ISO, locale));
        }

        // TODO check OPC_KEY : payment 0 EUR fail si errorCode = 500 + "error_label":"Business Transaction  ..."
        return errors;
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
