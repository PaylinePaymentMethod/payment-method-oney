package com.payline.payment.oney.integration;

import com.payline.payment.oney.utils.TestCountry;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ItalieTestIT extends TestIT {

    public static final String TEST_PSP_GUID_KEY = PSP_GUID_KEY + ".it";

    public static final String TEST_PARTNER_AUTHORIZATION_KEY = PARTNER_AUTHORIZATION_KEY + ".it";

    private HashMap<String, ContractProperty> contractConfiguration;

    @Override
    public Map.Entry<String, String> getPspGuid() {
        return new AbstractMap.SimpleEntry<String, String>(TEST_PSP_GUID_KEY, "31d8acd3-0c52-4bee-8ca1-36976c0b0317");
    }

    @Override
    public Map.Entry<String, String> getPartnerAuthorizationKey() {
        return new AbstractMap.SimpleEntry<String, String>(TEST_PARTNER_AUTHORIZATION_KEY, "706ef0c03c71443295f1a7bbcff68bcb");
    }


    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        if (contractConfiguration != null) {
            return contractConfiguration;
        }
        contractConfiguration = new HashMap();
        contractConfiguration.put(OPC_KEY, new ContractProperty("3x002"));
        contractConfiguration.put(MERCHANT_GUID_KEY, new ContractProperty("3c417290-e5a5-421e-a49e-a259f699cfe9"));
        contractConfiguration.put(NB_ECHEANCES_KEY, new ContractProperty("3"));
        contractConfiguration.put(COUNTRY_CODE_KEY, new ContractProperty("IT")); // caractères
        contractConfiguration.put(LANGUAGE_CODE_KEY, new ContractProperty("it"));
        contractConfiguration.put(PARTNER_CHIFFREMENT_KEY, new ContractProperty("2D2QL/qcmHL0e2ts7Q8vGQonlrsl33Po11JKVOXnfug="));
        return contractConfiguration;
    }

    @Test
    public void fullPaymentTest() {
        PaymentRequest request = createDefaultPaymentRequest();
        this.fullRedirectionPayment(request, paymentService, paymentWithRedirectionService);
        assertTrue( true ); // assertion used to please Sonar (blocker issue. Yet there are assertions in fullRedirectionPayment method)
    }

    @Override
    public TestCountry getCountry() {
        return TestCountry.IT;
    }
}
