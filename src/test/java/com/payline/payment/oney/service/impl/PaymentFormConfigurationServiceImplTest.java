package com.payline.payment.oney.service.impl;

import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.ContractProperty;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.paymentform.bean.PaymentFormLogo;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.paymentform.response.configuration.impl.PaymentFormConfigurationResponseSpecific;
import com.payline.pmapi.bean.paymentform.response.logo.PaymentFormLogoResponse;
import com.payline.pmapi.bean.paymentform.response.logo.impl.PaymentFormLogoResponseFile;
import com.payline.pmapi.integration.AbstractPaymentIntegration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Currency;
import java.util.Locale;

import static com.payline.payment.oney.utils.OneyConstants.NB_ECHEANCES_KEY;
import static com.payline.payment.oney.utils.TestUtils.*;
import static org.mockito.Mockito.when;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PaymentFormConfigurationServiceImplTest {


    @InjectMocks
    private PaymentFormConfigurationServiceImpl service;

    @BeforeAll
    public void setup() {
        service = new PaymentFormConfigurationServiceImpl();
        MockitoAnnotations.initMocks(this);
    }


    @Test
    void testGetPaymentFormConfiguration() {
        final Environment environment = new Environment("http://google.com/", AbstractPaymentIntegration.SUCCESS_URL, "http://localhost/cancelurl.com/", true);
        final String nbEcheances = "99x";
        ContractConfiguration contractConfiguration = createContractConfiguration();
        contractConfiguration.getContractProperties().put(NB_ECHEANCES_KEY, new ContractProperty(nbEcheances));
        //Create a form config request
        PaymentFormConfigurationRequest paymentFormConfigurationRequest = PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withLocale(Locale.FRANCE)
                .withBuyer(createDefaultBuyer())
                .withAmount(new Amount(null, Currency.getInstance("EUR")))
                .withContractConfiguration(contractConfiguration)
                .withOrder(createCompleteOrder("007"))
                .withEnvironment(environment)
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();

        PaymentFormConfigurationResponseSpecific paymentFormConfigurationResponse = (PaymentFormConfigurationResponseSpecific) service.getPaymentFormConfiguration(paymentFormConfigurationRequest);

        Assertions.assertNotNull(paymentFormConfigurationResponse.getPaymentForm());
        Assertions.assertEquals("Payer avec Oney " + nbEcheances, paymentFormConfigurationResponse.getPaymentForm().getButtonText());
        Assertions.assertEquals("En sélectionnant ce moyen de paiement, vous serez redirigé sur le site dOney afin de faciliter votre paiement.", paymentFormConfigurationResponse.getPaymentForm().getDescription());
        Assertions.assertTrue(paymentFormConfigurationResponse.getPaymentForm().isDisplayButton());
    }

    @Test
    void testGetPaymentFormLogo() {
        //Mock PaymentFormLogoRequest
        PaymentFormLogoRequest paymentFormLogoRequest = Mockito.mock(PaymentFormLogoRequest.class);
        when(paymentFormLogoRequest.getLocale()).thenReturn(Locale.FRANCE);

        PaymentFormLogoResponse paymentFormLogoResponse = service.getPaymentFormLogo(paymentFormLogoRequest);

        Assertions.assertNotNull(paymentFormLogoResponse);
        Assertions.assertTrue(paymentFormLogoResponse instanceof PaymentFormLogoResponseFile);

        PaymentFormLogoResponseFile casted = (PaymentFormLogoResponseFile) paymentFormLogoResponse;
        Assertions.assertEquals(17, casted.getHeight());
        Assertions.assertEquals(54, casted.getWidth());
    }

    @Test
    void testGetLogo() {
        // when: getLogo is called
        String paymentMethodIdentifier = "Oney";
        PaymentFormLogo paymentFormLogo = service.getLogo(paymentMethodIdentifier, Locale.FRANCE);


        // then: returned elements are not null
        Assertions.assertNotNull(paymentFormLogo.getFile());
        Assertions.assertNotNull(paymentFormLogo.getContentType());

    }

}
