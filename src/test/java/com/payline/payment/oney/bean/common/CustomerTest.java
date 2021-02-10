package com.payline.payment.oney.bean.common;

import com.payline.payment.oney.bean.common.customer.Customer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.utils.BeanUtils.*;
import static com.payline.payment.oney.utils.TestUtils.createDefaultPaymentRequest;

public class CustomerTest {

    private Customer customer;


    @Test
    public void customerOK() throws Exception {
        customer = Customer.Builder.aCustomBuilder()
                .withCustomerIdentity(createDefaultCustomerIdentity())
                .withLanguageCode("FR")
                .withCustomerExternalCode("code")
                .withTrustFlag(1)
                .withContactDetails(createDefaultContactDetails())
                .withCustomerAddress(createDefaultCustomerAdress())
                .build();

        Assertions.assertNotNull(customer);
    }

    @Test
    public void testToString() throws Exception {
        customer = createDefaultCustomer();

        Assertions.assertTrue(customer.toString().contains("customer_external_code"));
        Assertions.assertTrue(customer.toString().contains("language_code"));
        Assertions.assertTrue(customer.toString().contains("identity"));
        Assertions.assertTrue(customer.toString().contains("contact_details"));
        Assertions.assertTrue(customer.toString().contains("customer_address"));
    }


}
