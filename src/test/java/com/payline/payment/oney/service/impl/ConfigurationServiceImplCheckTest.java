package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.exception.HttpCallException;
import com.payline.payment.oney.utils.TestUtils;
import com.payline.payment.oney.utils.http.OneyHttpClient;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

/**
 * Validates the {@link ConfigurationServiceImpl#check(ContractParametersCheckRequest)} method.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfigurationServiceImplCheckTest {

    @InjectMocks
    private ConfigurationServiceImpl service;

    @Spy
    OneyHttpClient httpClient;

    private Environment environment;
    private Map<String, ContractProperty> contractProperties;
    private Map<String, String> partnerConfiguration;
    private Map<String, String> sensitivePartnerConfiguration;

    @BeforeAll
    public void setUpAll() throws HttpCallException {
        this.service = new ConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);

        // Mock environment
        this.environment = new Environment(
                "http://google.com/",
                "https://succesurl.com/",
                "http://localhost/cancelurl.com/",
                true
        );

        // Mock HTTP call to the partner API.
        StringResponse mockResponsePending = createStringResponse(200, "OK", "{\"encrypted_message\":\"+l2i0o7hGRh+wJO02++ul3aakmok0anPtpBvW1vZ3e83c7evaIMgKsuqlJpPjg407AoMkFm94736cZcnpC81qiX4V8n9IxMD1E50QBAOkMZ1S8Pf90kxhXSDe3wt4J13\"}");
        Mockito.doReturn(mockResponsePending).when(httpClient).initiateCheckPayment(Mockito.anyString(), Mockito.anyMap());
    }

    @BeforeEach
    public void setUp(){
        // Mock full contract properties
        contractProperties = new HashMap<>();
        contractProperties.put(MERCHANT_GUID_KEY, new ContractProperty("merchant_guid_test"));
        contractProperties.put(OPC_KEY, new ContractProperty("3z002"));
        contractProperties.put(NB_ECHEANCES_KEY, new ContractProperty("nx"));
        contractProperties.put(COUNTRY_CODE_KEY, new ContractProperty("BE"));
        contractProperties.put(LANGUAGE_CODE_KEY, new ContractProperty("fr"));
        contractProperties.put(PARTNER_CHIFFREMENT_KEY, new ContractProperty("1234567890ABCDEF="));

        // Mock full partner configuration
        partnerConfiguration = new HashMap<>();
        partnerConfiguration.put(PSP_GUID_KEY + ".be", "psp_id_test");
        partnerConfiguration.put(SECRET_KEY, TestUtils.getSecretKey());
        partnerConfiguration.put(PARTNER_AUTHORIZATION_KEY + ".be", "mykey");
        partnerConfiguration.put(PARTNER_API_URL, "https://oney-staging.azure-api.net");

        // Mock sensitive partner configuration
        sensitivePartnerConfiguration = new HashMap<>();
    }

    @Test
    public void check_ok(){
        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: no error
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void check_missingMerchantGuid(){
        // given: no merchant guid is provided
        contractProperties.put(MERCHANT_GUID_KEY, new ContractProperty(""));

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: there is an error on the merchant guid field
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(MERCHANT_GUID_KEY));
    }

    @Test
    public void check_missingOpc(){
        // given: no OPC is provided
        contractProperties.put(OPC_KEY, new ContractProperty(""));

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: there is an error on the OPC field
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(OPC_KEY));
    }

    @Test
    public void check_missingCountryCode(){
        // given: no country code is provided
        contractProperties.remove(COUNTRY_CODE_KEY);

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: there is an error on the country code field
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(COUNTRY_CODE_KEY));
    }

    @Test
    public void check_nonIsoCountryCode(){
        // given: a non-iso country code
        contractProperties.put(COUNTRY_CODE_KEY, new ContractProperty("BEL"));

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: there is an error on the country code field
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(COUNTRY_CODE_KEY));
    }

    @Test
    public void check_nonIsoMerchantLanguageCode(){
        // given: a non-iso merchant language code
        contractProperties.put(LANGUAGE_CODE_KEY, new ContractProperty("fra"));

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: there is an error on the merchant language code field
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(LANGUAGE_CODE_KEY));
    }

    @Test
    public void check_missingPspGuid(){
        // given: empty PSP guid
        partnerConfiguration.put(PSP_GUID_KEY + ".be", "");

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: error on the PSP guid
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(PSP_GUID_KEY));
    }

    @Test
    public void check_missingPartnerAuthorizationKey(){
        // given: empty partner authorization key
        partnerConfiguration.put(PARTNER_AUTHORIZATION_KEY + ".be", "");

        // when: calling check method
        Map<String, String> errors = service.check( this.createContractParametersCheckRequest() );

        // then: error on the partner authorization key
        Assertions.assertEquals(1, errors.size());
        Assertions.assertTrue(errors.containsKey(PARTNER_AUTHORIZATION_KEY));
    }

    // ---------- Below: private utility methods ----------

    /**
     * Instantiates a {@link ContractParametersCheckRequest} with the current test data.
     * @return A ContractParametersCheckRequest instance.
     */
    private ContractParametersCheckRequest createContractParametersCheckRequest(){
        return ContractParametersCheckRequest.CheckRequestBuilder
                .aCheckRequest()
                .withAccountInfo( getAccountInfo() )
                .withLocale( Locale.FRANCE )
                .withPartnerConfiguration( new PartnerConfiguration(partnerConfiguration, sensitivePartnerConfiguration) )
                .withContractConfiguration( new ContractConfiguration("Oney", contractProperties) )
                .withEnvironment( environment )
                .build();
    }

    /**
     * Generates an accountInfo map from contractConfiguration property.
     * @return The created map.
     */
    private Map<String, String> getAccountInfo(){
        Map<String, String> accountInfo = new HashMap<>();
        for (String key : contractProperties.keySet()) {
            accountInfo.put(key, contractProperties.get(key).getValue());
        }
        return accountInfo;
    }
}
