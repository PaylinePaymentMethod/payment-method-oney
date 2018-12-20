package com.payline.payment.oney.utils;

import com.payline.payment.oney.utils.http.StringResponse;
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
import org.apache.logging.log4j.LogManager;
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

    private static final String PHONE_NUMBER_TEST = "06060606";
    public HashMap<String, String> extendedData;
    private static final String SOFT_DESCRIPTOR = "softDescriptor";
    private static final String MERCHANT_REQUEST_ID = createMerchantRequestId();
    public static final String CONFIRM_AMOUNT = "456";
    private static final String TRANSACTION_ID = "455454545415451198120";
    private static final String CONFIRM_EXTERNAL_REFERENCE = "CMD|" + TRANSACTION_ID;


    /**
     * Create a paymentRequest with default parameters.
     *
     * @return paymentRequest created
     */
    public static PaymentRequest createDefaultPaymentRequest() {
        final Amount amount = createAmount(CONFIRM_AMOUNT,"EUR");
        final ContractConfiguration contractConfiguration = createContractConfiguration();
        final Environment paylineEnvironment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final Order order = createOrder(TRANSACTION_ID);


        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withLocale(Locale.FRANCE)
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(paylineEnvironment)
                .withOrder(order)
                .withBuyer(createDefaultBuyer())
                .withTransactionId(TRANSACTION_ID)
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withEnvironment(createDefaultEnvironment())
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }


    /**
     * Create a default form context for Unit Test and IT Test
     *
     * @return PaymentFormContext which contain a mobile phone number and a iban
     */
    public static PaymentFormContext createDefaultPaymentFormContext(String phoneNumber) {
        Map<String, String> paymentFormParameter = new HashMap<>();
        paymentFormParameter.put(PSP_GUID_KEY, "6ba2a5e2-df17-4ad7-8406-6a9fc488a60a");

        Map<String, String> sensitivePaymentFormParameter = new HashMap<>();
        sensitivePaymentFormParameter.put(SECRET_KEY, "6ba2a5e2-df17-4ad7-8406-6a9fc488a60a");

        return PaymentFormContext.PaymentFormContextBuilder
                .aPaymentFormContext()
                .withPaymentFormParameter(paymentFormParameter)
                .withSensitivePaymentFormParameter(sensitivePaymentFormParameter)
                .build();

    }


    public static RefundRequest createRefundRequest(String transactionId) {
        final Environment paylineEnvironment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
//       final String transactionID = createTransactionId();
        final Amount amount = createAmount(CONFIRM_AMOUNT,"EUR");
        final Map<String, String> partnerConfiguration = new HashMap<>();
        final Map<String, String> sensitiveConfig = new HashMap<>();
        return RefundRequest.RefundRequestBuilder.aRefundRequest()
                .withAmount(amount)
                .withOrder(createOrder(transactionId, amount))
                .withBuyer(createDefaultBuyer())
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(paylineEnvironment)
                .withTransactionId(transactionId)
                .withPartnerTransactionId("toto")
                .withPartnerConfiguration(new PartnerConfiguration(partnerConfiguration, sensitiveConfig))
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
        final Amount amount = createAmount(CONFIRM_AMOUNT,"EUR");
        final ContractConfiguration contractConfiguration = createContractConfiguration();

        final Environment paylineEnvironment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = createTransactionId();
        final Order order = createOrder(transactionID);
        final Locale locale = new Locale("FR");

        return PaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withPaymentFormContext(createDefaultPaymentFormContext(PHONE_NUMBER_TEST))
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .withLocale(Locale.FRANCE)
                .withBuyer(createDefaultBuyer());
    }

    //Cree une redirection payment par defaut
    public static RedirectionPaymentRequest createCompleteRedirectionPaymentBuilder() {
        final Amount amount =  createAmount(CONFIRM_AMOUNT,"EUR");
        final ContractConfiguration contractConfiguration = createContractConfiguration();

        final Environment paylineEnvironment = new Environment(NOTIFICATION_URL, SUCCESS_URL, CANCEL_URL, true);
        final String transactionID = MERCHANT_REQUEST_ID;
        final Order order = createOrder(transactionID);
        final Locale locale = new Locale("FR");

        Map<String, String> requestData = new HashMap<>();
        requestData.put(PSP_GUID_KEY, GUID_KEY);
        requestData.put(SECRET_KEY, "Method-body");
        requestData.put(EXTERNAL_REFERENCE_KEY, CONFIRM_EXTERNAL_REFERENCE);


        final RequestContext requestContext = RequestContext.RequestContextBuilder
                .aRequestContext()
                .withRequestData(requestData)
                .build();
        return (RedirectionPaymentRequest) RedirectionPaymentRequest.builder()
                .withAmount(amount)
                .withBrowser(new Browser("", Locale.FRANCE))
                .withContractConfiguration(contractConfiguration)
                .withEnvironment(paylineEnvironment)
                .withOrder(order)
                .withLocale(locale)
                .withTransactionId(transactionID)
                .withSoftDescriptor(SOFT_DESCRIPTOR)
                .withPaymentFormContext(createDefaultPaymentFormContext(PHONE_NUMBER_TEST))
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .withLocale(Locale.FRANCE)
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

    public static Map<Buyer.AddressType, Address> createDefaultAddresses() {
        Address address = createDefaultCompleteAddress();
        return createAddresses(address);
    }

    public static Amount createAmount(String currency) {
        return new Amount(BigInteger.TEN, Currency.getInstance(currency));
    }
    public static Amount createAmount(String amount, String currency) {
       return new Amount ( new BigInteger(amount), Currency.getInstance(currency));
    }

    public static Order createOrder(String transactionID) {

        List<Order.OrderItem> orderItems = new ArrayList<>();
        orderItems.add(createOrderItem("item1", createAmount("EUR")));
        orderItems.add(createOrderItem("item2", createAmount("EUR")));
        return Order.OrderBuilder.anOrder()
                .withReference(transactionID)
                .withAmount(createAmount(CONFIRM_AMOUNT,"EUR"))
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
        return new Buyer.FullName("Robinho", "Hoodyle", "4");
    }

    public static Map<Buyer.PhoneNumberType, String> createDefaultPhoneNumbers() {
        Map<Buyer.PhoneNumberType, String> phoneNumbers = new HashMap<>();
        phoneNumbers.put(Buyer.PhoneNumberType.BILLING, "0606060607");
        phoneNumbers.put(Buyer.PhoneNumberType.CELLULAR, "+320625262428");
        phoneNumbers.put(Buyer.PhoneNumberType.HOME, "0708070704");
        phoneNumbers.put(Buyer.PhoneNumberType.UNDEFINED, "0708070709");
        phoneNumbers.put(Buyer.PhoneNumberType.WORK, "0708070709");

        return phoneNumbers;
    }

    public static ContractConfiguration createContractConfiguration() {
        final ContractConfiguration contractConfiguration = new ContractConfiguration("Oney", new HashMap<>());
        contractConfiguration.getContractProperties().put(BUSINESS_TRANSACTION_CODE, new ContractProperty("3x002"));
        contractConfiguration.getContractProperties().put(MERCHANT_GUID_KEY, new ContractProperty("9813e3ff-c365-43f2-8dca-94b850befbf9"));
        contractConfiguration.getContractProperties().put(PSP_GUID_KEY, new ContractProperty(GUID_KEY));
        contractConfiguration.getContractProperties().put(API_MARKETING_KEY, new ContractProperty("01c6ea9021574d608c631f1c3b880b3be"));
        contractConfiguration.getContractProperties().put(OPC_KEY, new ContractProperty("3x002"));
        contractConfiguration.getContractProperties().put(NB_ECHEANCES_KEY, new ContractProperty("2"));
        contractConfiguration.getContractProperties().put(COUNTRY_CODE_KEY, new ContractProperty("BEL")); // ouy 3 caractères
        contractConfiguration.getContractProperties().put(LANGUAGE_CODE_KEY, new ContractProperty("FR"));
        contractConfiguration.getContractProperties().put(ID_INTERNATIONAL_KEY, new ContractProperty("FR"));
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
                .withEnvironment(createDefaultEnvironment())
                .withLocale(Locale.FRANCE)
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }


    public static Address createAddress(String street, String city, String zip) {
        return Address.AddressBuilder.anAddress()
                .withStreet1(street)
                .withCity(city)
                .withZipCode(zip)
                .withCountry("BE")
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

    public static Address createDefaultAddress() {
        return createAddress("12 a street", "Paris", "75000");
    }

    public static Address createDefaultCompleteAddress() {
        return createCompleteAddress("120 rue neuve", "residence azert", "Bruxelles", "1000", "BE");
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

    private static String generateRamdomEmail() {

        return "testoney" + Calendar.getInstance().getTimeInMillis() + "@gmail.com";
    }

    private static Date getBirthdayDate() {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse("04/05/1991");
        } catch (ParseException e) {
            LOGGER.error("parsing de la date de naissance impossible", e);
            return null;
        }
    }


    public static Map<String, String> createDefaultExtendedData() {

        return new HashMap<String, String>();

    }

    public static Buyer createDefaultBuyer() {
        return createBuyer(createDefaultPhoneNumbers(), createDefaultAddresses(), createFullName());
    }


    public static Environment createDefaultEnvironment() {
//        return new Environment("http://notificationURL.com", "http://redirectionURL.com", "http://redirectionCancelURL.com", true);
        return new Environment("https://succesurl.com/", "http://redirectionURL.com", "http://redirectionCancelURL.com", true);
    }

    public static PartnerConfiguration createDefaultPartnerConfiguration() {
        Map<String, String> partnerConfiguration = new HashMap<>();
        partnerConfiguration.put(PSP_GUID_KEY, GUID_KEY);
        partnerConfiguration.put(SECRET_KEY, "Method-body");

        Map<String, String> sensitivePartnerConfiguration = new HashMap<>();
        sensitivePartnerConfiguration.put(CHIFFREMENT_KEY, "66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=");


        return new PartnerConfiguration(partnerConfiguration, sensitivePartnerConfiguration);
    }

    public static PaymentFormConfigurationRequest createDefaultPaymentFormConfigurationRequest() {
        return PaymentFormConfigurationRequest.PaymentFormConfigurationRequestBuilder.aPaymentFormConfigurationRequest()
                .withLocale(Locale.FRANCE)
                .withBuyer(createDefaultBuyer())
                .withAmount(createAmount(CONFIRM_AMOUNT,"EUR"))
                .withContractConfiguration(createContractConfiguration())
                .withOrder(createOrder("007"))
                .withEnvironment(createDefaultEnvironment())
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


    public static TransactionStatusRequest createDefaultTransactionStatusRequest(){
        return TransactionStatusRequest.TransactionStatusRequestBuilder
                .aNotificationRequest()
                .withTransactionId(TRANSACTION_ID)
                .withAmount(createAmount(CONFIRM_AMOUNT,"EUR"))
                .withContractConfiguration(createContractConfiguration())
                .withEnvironment(createDefaultEnvironment())
                .withOrder(createOrder(TRANSACTION_ID))
                .withBuyer(createDefaultBuyer())
                .withPartnerConfiguration(createDefaultPartnerConfiguration())
                .build();
    }

    public static String RandomSecondName() {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return  buffer.toString();
    }
}
