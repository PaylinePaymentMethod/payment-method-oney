package com.payline.payment.oney.service;

import com.payline.payment.oney.service.impl.ConfigurationServiceImpl;
import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.TestUtils.createContractConfiguration;
import static com.payline.payment.oney.utils.TestUtils.createDefaultEnvironment;

public class ConfigurationServiceImplTest {

    private ConfigurationServiceImpl service = new ConfigurationServiceImpl();

    @Test
    public void testGetParameters() {

        List<AbstractParameter> parameters = service.getParameters(Locale.FRANCE);
        //Assert we have 3 parameters
        Assertions.assertNotNull(parameters);
        Assertions.assertEquals(9, parameters.size());
        Assertions.assertEquals(X_ONEY_AUTHORIZATION_KEY, parameters.get(1).getKey());
        Assertions.assertEquals(PSP_GUID_KEY, parameters.get(2).getKey());
        Assertions.assertEquals(MERCHANT_GUID_KEY, parameters.get(0).getKey());

    }

    @Test
    public void checkOK() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(X_ONEY_AUTHORIZATION_KEY, "mykey");
        accountInfo.put(PSP_GUID_KEY, "psp_id_test");
        accountInfo.put(MERCHANT_GUID_KEY, "merchant_guid_test");
        accountInfo.put(COUNTRY_CODE_KEY, "FR");
        accountInfo.put(LANGUAGE_CODE_KEY, "FR");


        ContractParametersCheckRequest contractParametersCheckRequest = ContractParametersCheckRequest.CheckRequestBuilder
                .aCheckRequest()
                .withAccountInfo(accountInfo)
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(createDefaultEnvironment())
                .build();

        Map<String, String> errors = service.check(contractParametersCheckRequest);
        Assertions.assertEquals(0, errors.size());

    }

    @Test
    public void checkCountryKO() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(X_ONEY_AUTHORIZATION_KEY, "mykey");
        accountInfo.put(PSP_GUID_KEY, "psp_id_test");
        accountInfo.put(MERCHANT_GUID_KEY, "merchant_guid_test");


        ContractParametersCheckRequest contractParametersCheckRequest = ContractParametersCheckRequest.CheckRequestBuilder
                .aCheckRequest()
                .withAccountInfo(accountInfo)
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(createDefaultEnvironment())
                .build();
        contractParametersCheckRequest.getContractConfiguration().getContractProperties().put(COUNTRY_CODE_KEY, new ContractProperty("BEL"));
        Map<String, String> errors = service.check(contractParametersCheckRequest);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.keySet().contains(COUNTRY_CODE_DESCRIPTION));
    }

    @Test
    public void checkLanguageKO() {

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put(X_ONEY_AUTHORIZATION_KEY, "mykey");
        accountInfo.put(PSP_GUID_KEY, "psp_id_test");
        accountInfo.put(MERCHANT_GUID_KEY, "merchant_guid_test");


        ContractParametersCheckRequest contractParametersCheckRequest = ContractParametersCheckRequest.CheckRequestBuilder
                .aCheckRequest()
                .withAccountInfo(accountInfo)
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(createDefaultEnvironment())
                .build();
        contractParametersCheckRequest.getContractConfiguration().getContractProperties().put(LANGUAGE_CODE_KEY, new ContractProperty("FR"));
        Map<String, String> errors = service.check(contractParametersCheckRequest);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.keySet().contains(LANGUAGE_CODE_KEY));
    }

    @Test
    public void checkKO() {

        Map<String, String> accountInfo = new HashMap<>();

        ContractParametersCheckRequest contractParametersCheckRequest = ContractParametersCheckRequest.CheckRequestBuilder
                .aCheckRequest()
                .withAccountInfo(accountInfo)
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(createDefaultEnvironment())
                .build();
        Map<String, String> errors = service.check(contractParametersCheckRequest);
        Assertions.assertEquals(3, errors.size());
        Assertions.assertNotNull(errors.get(X_ONEY_AUTHORIZATION_KEY));
        Assertions.assertNotNull(errors.get(PSP_GUID_KEY));
        Assertions.assertNotNull(errors.get(MERCHANT_GUID_KEY));
    }

    @Test
    public void testGetReleaseInformation_ok() {
        // when: getReleaseInformation method is called
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: result is not null
        Assertions.assertNotNull(releaseInformation);
        // then: assert release version and release date are not null
        Assertions.assertNotNull(releaseInformation.getVersion());
        Assertions.assertFalse(releaseInformation.getVersion().isEmpty());
        Assertions.assertNotNull(releaseInformation.getDate());
    }

    //
    @Test
    public void testGetReleaseInformation_versionFormat() {
        // when: getReleaseInformation method is called
        ReleaseInformation releaseInformation = service.getReleaseInformation();

        // then: the version has a valid format
        Assertions.assertNotNull(releaseInformation);
        Assertions.assertTrue(releaseInformation.getVersion().matches("^\\d\\.\\d(\\.\\d)?$"));
    }

}
