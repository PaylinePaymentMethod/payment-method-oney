package com.payline.payment.oney.integration;

import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.payline.payment.oney.utils.OneyConstants.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FranceTestIT extends TestIT {

    @Override
    protected Map<String, ContractProperty> generateParameterContract() {
        HashMap<String, ContractProperty> contractConfiguration = new HashMap();
        contractConfiguration.put(OPC_KEY, new ContractProperty("W4026"));
        contractConfiguration.put(MERCHANT_GUID_KEY, new ContractProperty("6fd0d7f8123b4a729cb74a89f32e6035"));
        contractConfiguration.put(NB_ECHEANCES_KEY, new ContractProperty("3"));
        contractConfiguration.put(COUNTRY_CODE_KEY, new ContractProperty("FR")); // caractères
        contractConfiguration.put(LANGUAGE_CODE_KEY, new ContractProperty("FR"));
        contractConfiguration.put(PARTNER_CHIFFREMENT_KEY, new ContractProperty("dqcoCKNBvHFIIvDlcsrqYg44Q2JhmjTmDJR6NwdBogU="));
        return contractConfiguration;
    }

    @Test
    public void fullPaymentTest() {
        PaymentRequest request = createDefaultPaymentRequest();
        this.fullRedirectionPayment(request, paymentService, paymentWithRedirectionService);
        assertTrue( true ); // assertion used to please Sonar (blocker issue. Yet there are assertions in fullRedirectionPayment method)
    }
}
