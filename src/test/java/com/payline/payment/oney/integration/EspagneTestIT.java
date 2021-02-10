package com.payline.payment.oney.integration;

import com.payline.payment.oney.utils.TestCountry;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;

public class EspagneTestIT extends TestIT {

    public static final String TEST_PSP_GUID_KEY = PSP_GUID_KEY + ".es";

    public static final String TEST_PARTNER_AUTHORIZATION_KEY = PARTNER_AUTHORIZATION_KEY + ".es";

    private HashMap<String, ContractProperty> contractConfiguration;

    @Override
    public Map.Entry<String, String> getPspGuid() {
        return null;
    }

    @Override
    public Map.Entry<String, String> getPartnerAuthorizationKey() {
        return null;
    }

    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        if (contractConfiguration != null) {
            return contractConfiguration;
        }
        contractConfiguration = new HashMap();
//        contractConfiguration.put(OPC_KEY, new ContractProperty("3x002"));
//        contractConfiguration.put(MERCHANT_GUID_KEY, new ContractProperty("9813e3ff-c365-43f2-8dca-94b850befbf9"));
        contractConfiguration.put(NB_ECHEANCES_KEY, new ContractProperty("3"));
        contractConfiguration.put(COUNTRY_CODE_KEY, new ContractProperty("ES")); // caractères
        contractConfiguration.put(LANGUAGE_CODE_KEY, new ContractProperty("es"));
//        contractConfiguration.put(PARTNER_CHIFFREMENT_KEY, new ContractProperty("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY="));
        return contractConfiguration;
    }

    //    @Test
    public void fullPaymentTest() {
        PaymentRequest request = createDefaultPaymentRequest();
        this.fullRedirectionPayment(request, paymentService, paymentWithRedirectionService);

    }

    @Override
    public TestCountry getCountry() {
        return TestCountry.ES;
    }
}
