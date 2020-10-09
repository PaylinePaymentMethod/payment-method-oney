package com.payline.payment.oney.integration;

import com.payline.payment.oney.utils.TestCountry;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;

public class PortugalTestIT extends TestIT {

    public static final String TEST_PSP_GUID_KEY = PSP_GUID_KEY + ".pt";

    public static final String TEST_PARTNER_AUTHORIZATION_KEY = PARTNER_AUTHORIZATION_KEY + ".pt";

    private HashMap<String, ContractProperty> contractConfiguration;

    @Override
    public Map.Entry<String, String> getPspGuid() {
        return new AbstractMap.SimpleEntry<String, String>(TEST_PSP_GUID_KEY, "6e92d305-c302-4092-8419-e91931a9be61");
    }

    @Override
    public Map.Entry<String, String> getPartnerAuthorizationKey() {
        return new AbstractMap.SimpleEntry<String, String>(TEST_PARTNER_AUTHORIZATION_KEY, "8dc3e1a2ab7a44afa8e72c741e75af1c");
    }

    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        if (contractConfiguration != null) {
            return contractConfiguration;
        }
        contractConfiguration = new HashMap();
        contractConfiguration.put(OPC_KEY, new ContractProperty("3x002"));
        contractConfiguration.put(MERCHANT_GUID_KEY, new ContractProperty("68299636-694f-4a04-aef4-14c27647ca24"));
        contractConfiguration.put(NB_ECHEANCES_KEY, new ContractProperty("3x"));
        contractConfiguration.put(COUNTRY_CODE_KEY, new ContractProperty("PT")); // caractères
        contractConfiguration.put(LANGUAGE_CODE_KEY, new ContractProperty("pt"));
        contractConfiguration.put(PARTNER_CHIFFREMENT_KEY, new ContractProperty("aCY48dTCx4DSnEqgv8FkPHP6tRvfnYw2aIH/OI0uGUI="));
        return contractConfiguration;
    }

    @Test
    public void fullPaymentTest() {
        PaymentRequest request = createDefaultPaymentRequest();
        Assertions.assertDoesNotThrow(() -> this.fullRedirectionPayment(request, paymentService, paymentWithRedirectionService));
    }

    @Override
    public TestCountry getCountry() {
        return TestCountry.PT;
    }
}
