package com.payline.payment.oney.service.impl;

import com.payline.pmapi.bean.configuration.ReleaseInformation;
import com.payline.pmapi.bean.configuration.parameter.AbstractParameter;
import com.payline.pmapi.bean.configuration.parameter.impl.InputParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.payline.payment.oney.utils.OneyConstants.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigurationServiceImplTest {

    @InjectMocks
    private ConfigurationServiceImpl service;

    @BeforeAll
    public void setUp() throws Exception {
        service = new ConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetParametersKeys() {
        List<AbstractParameter> parameters = service.getParameters(Locale.FRANCE);
        Pattern p = Pattern.compile("[a-zA-Z\\.]*");
        for (AbstractParameter param : parameters) {
            Assertions.assertTrue(p.matcher(param.getKey()).matches(), param.getKey() + " comporte des caracteres interdits");
        }
    }

    @Test
    public void testGetParametersKeys_KO1() {
        // merchant GUID
        final InputParameter merchantGuid = new InputParameter();
        merchantGuid.setKey("test_test");
        merchantGuid.setLabel("test");
        merchantGuid.setDescription("test");
        merchantGuid.setRequired(true);

        List<AbstractParameter> parameters = new ArrayList<>();
        parameters.add(merchantGuid);

        Pattern p = Pattern.compile("[a-zA-Z\\.]*");
        for (AbstractParameter param : parameters) {
            Assertions.assertFalse(p.matcher(param.getKey()).matches(), param.getKey() + " comporte des caracteres interdits");
        }
    }

    @Test
    public void testGetParametersKeys_KO2() {
        // merchant GUID
        final InputParameter merchantGuid = new InputParameter();
        merchantGuid.setKey("test-test");
        merchantGuid.setLabel("test");
        merchantGuid.setDescription("test");
        merchantGuid.setRequired(true);

        List<AbstractParameter> parameters = new ArrayList<>();
        parameters.add(merchantGuid);

        Pattern p = Pattern.compile("[a-zA-Z\\.]*");
        for (AbstractParameter param : parameters) {
            Assertions.assertFalse(p.matcher(param.getKey()).matches(), param.getKey() + " comporte des caracteres interdits");
        }
    }

    @Test
    public void testGetParametersKeys_OkExt() {
        // merchant GUID
        final InputParameter merchantGuid = new InputParameter();
        merchantGuid.setKey("test.test");
        merchantGuid.setLabel("test");
        merchantGuid.setDescription("test");
        merchantGuid.setRequired(true);

        List<AbstractParameter> parameters = service.getParameters(Locale.FRANCE);
        parameters.add(merchantGuid);

        Pattern p = Pattern.compile("[a-zA-Z\\.]*");
        for (AbstractParameter param : parameters) {
            Assertions.assertTrue(p.matcher(param.getKey()).matches(), param.getKey() + " comporte des caracteres interdits");
        }
    }


    @Test
    public void testGetParameters() {
        List<AbstractParameter> parameters = service.getParameters(Locale.FRANCE);
        //Assert we have 3 parameters
        Assertions.assertNotNull(parameters);
        Assertions.assertEquals(7, parameters.size());

        List<String> result = new ArrayList<>();
        for (AbstractParameter paramter : parameters) {
            result.add(paramter.getKey());
        }
        Assertions.assertTrue(result.contains(MERCHANT_GUID_KEY));
        Assertions.assertTrue(result.contains(OPC_KEY));
        Assertions.assertTrue(result.contains(NB_ECHEANCES_KEY));
        Assertions.assertTrue(result.contains(COUNTRY_CODE_KEY));
        Assertions.assertTrue(result.contains(LANGUAGE_CODE_KEY));
        Assertions.assertTrue(result.contains(ID_INTERNATIONAL_KEY));

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
