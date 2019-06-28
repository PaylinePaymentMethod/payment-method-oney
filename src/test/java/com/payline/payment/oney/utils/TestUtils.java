package com.payline.payment.oney.utils;

import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.common.Buyer.Address;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.*;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.RedirectionPaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.integration.AbstractPaymentIntegration;
import com.payline.pmapi.logger.LogManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.payline.payment.oney.utils.OneyConstants.*;


/**
 * Class with method to generate mock easier
 */
public class TestUtils {
    private static final Logger LOGGER = LogManager.getLogger(TestUtils.class);

    private static final String SUCCESS_URL = AbstractPaymentIntegration.SUCCESS_URL;
    private static final String CANCEL_URL = "http://localhost/cancelurl.com/";
    private static final String NOTIFICATION_URL = "http://google.com/";
    private static final String GUID_KEY = "6ba2a5e2-df17-4ad7-8406-6a9fc488a60a";

    public static final String SOFT_DESCRIPTOR = "softDescriptor";
    public static final String MERCHANT_REQUEST_ID = createMerchantRequestId();
    public static final String CONFIRM_AMOUNT = "40800";
    private static final String TRANSACTION_ID = "455454545415451198120";
    private static final String EXTERNAL_REFERENCE = "123456789A";
    private static final String CONFIRM_EXTERNAL_REFERENCE = OneyConstants.EXTERNAL_REFERENCE_TYPE + PIPE + EXTERNAL_REFERENCE;

    private static final Currency CURRENCY_EUR = Currency.getInstance("EUR");
    private static final Locale LOCALE_FR = Locale.FRANCE;

    public static final Environment TEST_ENVIRONMENT = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);

    private static final String TEST_PSP_GUID_KEY = PSP_GUID_KEY + ".be";
    private static final String TEST_CHIFFREMENT_KEY = "\"66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=\"";
    private static final String TEST_PARTNER_AUTHORIZATION_KEY = PARTNER_AUTHORIZATION_KEY + ".be";

    private static String testPhonenumber = null;

    public static String getTestphoneNumber(TestCountry testCountry) {
        if (testPhonenumber == null) {
            testPhonenumber = testCountry.getIndicatifTel() + RandomStringUtils.random(9, false, true);
        }
        return testPhonenumber;
    }

    /**
     * Create a paymentRequest with default parameters.
     *
     * @return paymentRequest created
     */
    public static PaymentRequest createDefaultPaymentRequest() {
        final Amount amount = createAmount(CONFIRM_AMOUNT, CURRENCY_EUR);
        final ContractConfiguration contractConfiguration = createContractConfiguration();
        final Order order = createOrder(TRANSACTION_ID);


        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", LOCALE_FR))
                .withLocale(LOCALE_FR)
                .withContractConfiguration(contractConfiguration)
                .withOrder(order)
                .withBuyer(createDefaultBuyer())
                .withTransactionId(TRANSACTION_ID)
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withEnvironment(TEST_ENVIRONMENT)
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }


    /**
     * Create a default form context for Unit Test and IT Test
     *
     * @return PaymentFormContext which contain a mobile phone number and a iban
     */
    public static PaymentFormContext createDefaultPaymentFormContext() {
        Map<String, String> paymentFormParameter = new HashMap<>();
        paymentFormParameter.put(TEST_PSP_GUID_KEY, GUID_KEY);

        Map<String, String> sensitivePaymentFormParameter = new HashMap<>();

        return PaymentFormContext.PaymentFormContextBuilder
                .aPaymentFormContext()
                .withPaymentFormParameter(paymentFormParameter)
                .withSensitivePaymentFormParameter(sensitivePaymentFormParameter)
                .build();

    }

    public static CaptureRequest createDefaultCaptureRequest(){
        return  CaptureRequest.CaptureRequestBuilder.aCaptureRequest()
                .withTransactionId(TRANSACTION_ID)
                .withPartnerTransactionId(EXTERNAL_REFERENCE)
                .withAmount(TestUtils.createAmount(Currency.getInstance("EUR")))
                .withBuyer(TestUtils.createDefaultBuyer())
                .withContractConfiguration(TestUtils.createContractConfiguration())
                .withEnvironment(TestUtils.TEST_ENVIRONMENT)
                .withOrder(TestUtils.createOrder(TRANSACTION_ID))
                .withPartnerConfiguration(TestUtils.createDefaultPartnerConfiguration())
                .build();
    }


    public static RefundRequest createRefundRequest(String transactionId) {
//       final String transactionID = createTransactionId();
        final Amount amount = createAmount(CONFIRM_AMOUNT, CURRENCY_EUR);
        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withOrder(createOrder(transactionId, amount))
                .withBuyer(createDefaultBuyer())
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(TEST_ENVIRONMENT)
                .withTransactionId(transactionId)
                .withPartnerTransactionId("toto")
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }

    public static RedirectionPaymentRequest createRedirectionPaymentRequest() {
        return RedirectionPaymentRequest.builder().build();

    }

    /**
     * Create a complete payment request used for Integration Tests
     *
     * @return PaymentRequest.Builder
     */


    public static PaymentRequest.Builder createCompletePaymentBuilder() {
        return createCompletePaymentBuilder(TestCountry.BE, createContractConfiguration(),
                createDefaultPaymentFormContext(), createDefaultPartnerConfiguration());
    }

    public static PaymentRequest.Builder createCompletePaymentBuilder(
            TestCountry testCountry, ContractConfiguration contractConfiguration,
            PaymentFormContext paymentFormContext, PartnerConfiguration partnerConfiguration) {
        final Amount amount = createAmount(CONFIRM_AMOUNT, CURRENCY_EUR);

        final String transactionID = createTransactionId();
        Locale testLocale = null;
        Locale[] all = Locale.getAvailableLocales();
        for (Locale locale : all) {
            String country = locale.getCountry();
            if (country.equalsIgnoreCase(testCountry.name())) {
                testLocale = locale;
                break;
            }
        }
        final Order order = createOrder(transactionID);
        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", testLocale))
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(TEST_ENVIRONMENT)
                .withOrder(order)
                .withLocale(testLocale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withPaymentFormContext(paymentFormContext)
                .withPartnerConfiguration(partnerConfiguration)
                .withBuyer(createDefaultBuyer(testCountry));
    }

    //Cree une redirection payment par defaut
    public static RedirectionPaymentRequest createCompleteRedirectionPaymentBuilder() {
        final Amount amount = createAmount(CONFIRM_AMOUNT, CURRENCY_EUR);
        final ContractConfiguration contractConfiguration = createContractConfiguration();

        final String transactionID = MERCHANT_REQUEST_ID;
        final Order order = createOrder(transactionID);

        Map<String, String> requestData = new HashMap<>();
        requestData.put(TEST_PSP_GUID_KEY, GUID_KEY);
        requestData.put(SECRET_KEY, TestUtils.getSecretKey());
        requestData.put(EXTERNAL_REFERENCE_KEY, CONFIRM_EXTERNAL_REFERENCE);
        requestData.put(LANGUAGE_CODE_KEY, CONFIRM_EXTERNAL_REFERENCE);


        final RequestContext requestContext = RequestContext.RequestContextBuilder
                .aRequestContext()
                .withRequestData(requestData)
                .build();
        return RedirectionPaymentRequest.builder()
                .withCaptureNow(true)
                .withAmount(amount)
                .withBrowser(new Browser("", LOCALE_FR))
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(TEST_ENVIRONMENT)
                .withOrder(order)
                .withLocale(LOCALE_FR)
                .withTransactionId(transactionID)
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withPaymentFormContext(createDefaultPaymentFormContext())
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .withBuyer(createDefaultBuyer())
                //propre a la redirectionPayment
//                .withPaymentFormContext()
                .withRequestContext(requestContext)
                .build();


    }


    public static String createTransactionId() {
        return "141217" + Calendar.getInstance().getTimeInMillis();
    }

    public static String createMerchantRequestId() {
        return "131217" + Calendar.getInstance().getTimeInMillis();
    }


    public static Map<Buyer.AddressType, Address> createAddresses(Address address) {
        Map<Buyer.AddressType, Address> addresses = new HashMap<>();
        addresses.put(Buyer.AddressType.DELIVERY, address);
        addresses.put(Buyer.AddressType.BILLING, address);

        return addresses;
    }

    public static Map<Buyer.AddressType, Address> createDefaultAddresses(TestCountry testCountry) {
        Address address = createDefaultCompleteAddress(testCountry);
        return createAddresses(address);
    }

    public static Amount createAmount(Currency currency) {
        return new Amount(BigInteger.TEN, currency);
    }

    public static Amount createAmount(String amount, Currency currency) {
        return new Amount(new BigInteger(amount), currency);
    }

    public static Order createOrder(String transactionID) {

        List<Order.OrderItem> orderItems = new ArrayList<>();
        orderItems.add(createOrderItem("item1", createAmount(CURRENCY_EUR)));
        orderItems.add(createOrderItem("item2", createAmount(CURRENCY_EUR)));
        return Order.OrderBuilder.anOrder()
                .withReference(transactionID)
                .withAmount(createAmount(CONFIRM_AMOUNT, CURRENCY_EUR))
                .withDate(new Date())
                .withItems(orderItems)
                .withDeliveryMode("1")
                .withDeliveryTime("2")
                .withExpectedDeliveryDate(new Date())
                .build();
    }

    public static Order.OrderItem createOrderItem(String reference, Amount amount) {
        return Order.OrderItem.OrderItemBuilder.anOrderItem()
                .withAmount(amount)
                .withQuantity(4L)
                .withCategory("20001")
                .withComment("some label")
                .withBrand("someBrand")
                .withReference(reference)
                .withTaxRatePercentage(BigDecimal.TEN)
                .build();
    }

    public static Order createOrder(String transactionID, Amount amount) {
        return Order.OrderBuilder.anOrder().withReference(transactionID).withAmount(amount).build();
    }


    public static Buyer.FullName createFullName() {
        return new Buyer.FullName(
                RandomStringUtils.random(7, true, false),
                RandomStringUtils.random(10, true, false),
                "4");
    }

    public static Map<Buyer.PhoneNumberType, String> createDefaultPhoneNumbers(TestCountry testCountry) {
        Map<Buyer.PhoneNumberType, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, getTestphoneNumber(testCountry));
        phoneNumbers.put(Buyer.PhoneNumberType.CELLULAR, getTestphoneNumber(testCountry));
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, getTestphoneNumber(testCountry));
        phoneNumbers.put(Buyer.PhoneNumberType.UNDEFINED, getTestphoneNumber(testCountry));
        phoneNumbers.put(Buyer.PhoneNumberType.WORK, getTestphoneNumber(testCountry));

        return phoneNumbers;
    }

    public static ContractConfiguration createContractConfiguration() {
        final ContractConfiguration contractConfiguration = new ContractConfiguration("Oney", new HashMap<>());
        contractConfiguration.getContractProperties().put(MERCHANT_GUID_KEY, new ContractProperty("9813e3ff-c365-43f2-8dca-94b850befbf9"));
        contractConfiguration.getContractProperties().put(OPC_KEY, new ContractProperty("3x002"));
        contractConfiguration.getContractProperties().put(NB_ECHEANCES_KEY, new ContractProperty("2x"));
        contractConfiguration.getContractProperties().put(COUNTRY_CODE_KEY, new ContractProperty("BE")); // ouy 3 caractères
        contractConfiguration.getContractProperties().put(LANGUAGE_CODE_KEY, new ContractProperty("fr"));
        contractConfiguration.getContractProperties().put(ID_INTERNATIONAL_KEY, new ContractProperty("FR"));
        contractConfiguration.getContractProperties().put(PARTNER_CHIFFREMENT_KEY, new ContractProperty(TEST_CHIFFREMENT_KEY));
//used for Confirm

        return contractConfiguration;
    }

    public static Map<String, String> createAccountInfo() {

//        accountInfo.put(CONTRACT_CONFIG_CREDITOR_ID, GOOD_CREDITOR_ID);
//        accountInfo.put(PARTNER_CONFIG_AUTH_LOGIN, GOOD_LOGIN);
//        accountInfo.put(PARTNER_CONFIG_AUTH_PASS, GOOD_PWD);
        return new HashMap<String, String>();
    }

    public static ContractParametersCheckRequest createContractParametersCheckRequest() {

        return ContractParametersCheckRequest
                .CheckRequestBuilder
                .aCheckRequest()
                .withContractConfiguration(createContractConfiguration())
                .withAccountInfo(createAccountInfo())
                .withEnvironment(TEST_ENVIRONMENT)
                .withLocale(LOCALE_FR)
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }


    public static Address createCompleteAddress(String street, String street2, String city, String zip, String country) {
        return Address.AddressBuilder.anAddress()
                .withStreet1(street)
                .withStreet2(street2)
                .withCity(city)
                .withZipCode(zip)
                .withCountry(country)
                .withFullName(createFullName())
                .build();
    }


    public static Address createDefaultCompleteAddress(TestCountry testCountry) {
        return createCompleteAddress(RandomStringUtils.random(3, false, true)
                        + " rue " + RandomStringUtils.random(5, true, false),
                "residence " + RandomStringUtils.random(9
                        , true, false), "Bruxelles", "1000", testCountry.name());
    }

    public static Buyer createBuyer(Map<Buyer.PhoneNumberType, String> phoneNumbers, Map<Buyer.AddressType, Address> addresses, Buyer.FullName fullName) {
        return Buyer.BuyerBuilder.aBuyer()
                .withEmail(generateRamdomEmail())
                .withPhoneNumbers(phoneNumbers)
                .withAddresses(addresses)
                .withFullName(fullName)
                .withCustomerIdentifier("subscriber12")
                .withExtendedData(createDefaultExtendedData())
                .withBirthday(getBirthdayDate())
                .withLegalStatus(Buyer.LegalStatus.PERSON)
                .build();
    }

    public static String generateRamdomEmail() {

        return "testoney." + RandomStringUtils.random(5, true, false) + "@gmail.com";
    }

    private static Date getBirthdayDate() {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse("04/05/1981");
        } catch (ParseException e) {
            LOGGER.error("parsing de la date de naissance impossible", e);
            return null;
        }
    }


    public static Map<String, String> createDefaultExtendedData() {

        return new HashMap<String, String>();

    }

    public static Buyer createDefaultBuyer() {
        return createDefaultBuyer(TestCountry.BE);
    }

    public static Buyer createDefaultBuyer(TestCountry testCountry) {
        return createBuyer(createDefaultPhoneNumbers(testCountry), createDefaultAddresses(testCountry), createFullName());
    }

    public static PartnerConfiguration createDefaultPartnerConfiguration() {
        Map<String, String> partnerConfiguration = new HashMap<>();
        partnerConfiguration.put(TEST_PSP_GUID_KEY, GUID_KEY);
        partnerConfiguration.put(SECRET_KEY, TestUtils.getSecretKey());
        partnerConfiguration.put(TEST_PARTNER_AUTHORIZATION_KEY, "7fd3f1c53b9a47f7b85c801a32971895");
        partnerConfiguration.put(PARTNER_API_URL, "https://oney-staging.azure-api.net");

        Map<String, String> sensitivePartnerConfiguration = new HashMap<>();


        return new PartnerConfiguration(partnerConfiguration, sensitivePartnerConfiguration);
    }

    public static PaymentFormConfigurationRequest createDefaultPaymentFormConfigurationRequest() {
        return PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withLocale(LOCALE_FR)
                .withBuyer(createDefaultBuyer())
                .withAmount(createAmount(CONFIRM_AMOUNT, CURRENCY_EUR))
                .withContractConfiguration(createContractConfiguration())
                .withOrder(createOrder("007"))
                .withEnvironment(TEST_ENVIRONMENT)
                .withPartnerConfiguration(createDefaultPartnerConfiguration())

                .build();
    }

    public static StringResponse createStringResponse(int code, String message, String content) {
        StringResponse response = new StringResponse();
        response.setCode(code);
        response.setMessage(message);
        response.setContent(content);
        return response;
    }


    public static TransactionStatusRequest createDefaultTransactionStatusRequest() {
        return TransactionStatusRequest.TransactionStatusRequestBuilder
                .aNotificationRequest()
                .withTransactionId(TRANSACTION_ID)
                .withAmount(createAmount(CONFIRM_AMOUNT, CURRENCY_EUR))
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(TEST_ENVIRONMENT)
                .withOrder(createOrder(TRANSACTION_ID))
                .withBuyer(createDefaultBuyer())
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }

    public static RefundRequest createDefaultRefundRequest() {
        final Amount amount = createAmount(CONFIRM_AMOUNT, CURRENCY_EUR);
        final ContractConfiguration contractConfiguration = createContractConfiguration();
        final Order order = createOrder(TRANSACTION_ID);


        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withContractConfiguration(contractConfiguration)
                .withOrder(order)
                .withBuyer(createDefaultBuyer())
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withEnvironment(TEST_ENVIRONMENT)
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .withPartnerTransactionId(CONFIRM_EXTERNAL_REFERENCE)
                .withTransactionId(createTransactionId())
                .build();
    }

    public static String getSecretKey() {

        if (Boolean.valueOf(ConfigPropertiesEnum.INSTANCE.get(CHIFFREMENT_IS_ACTIVE))) {
            return SECRET_VALUE_ON;
        } else {
            return SECRET_VALUE_OFF;
        }
    }
}
