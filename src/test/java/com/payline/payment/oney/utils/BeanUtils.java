package com.payline.payment.oney.utils;

import com.payline.payment.oney.bean.common.OneyAddress;
import com.payline.payment.oney.bean.common.customer.ContactDetails;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.customer.CustomerIdentity;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.purchase.Delivery;
import com.payline.payment.oney.bean.common.purchase.Item;
import com.payline.payment.oney.bean.common.purchase.PurchaseMerchant;
import com.payline.payment.oney.bean.request.OneyConfirmRequest;
import com.payline.payment.oney.bean.request.OneyTransactionStatusRequest;
import com.payline.pmapi.bean.common.Buyer;

import java.util.ArrayList;
import java.util.List;

import static com.payline.payment.oney.utils.TestUtils.createDefaultBuyer;

public class BeanUtils {

    public static BusinessTransactionData createDefaultBusinessTransactionData(String code) {

        return BusinessTransactionData.Builder.aBusinessTransactionDataBuilder()
                .withCode(code)
                .withVersion(1)
                .withBusinessTransactionType("type")
                .build();
    }

    public static Customer createDefaultCustomer() {

        return Customer.Builder.aCustomBuilder()
                .withLanguageCode("FR")
                .withCustumerExternalCode("extCode")
                .withCustomerIdentity(createDefaultCustomerIdentity())
                .withContactDetails(createDefaultContactDetails())
                .withCustomerAddress(createDefaultCustomerAdress())
                .build();
    }

    public static ContactDetails createDefaultContactDetails() {
        return ContactDetails.Builder.aContactDetailsBuilder()
                .withLandLineNumber("0436656565")
                .withMobilePhoneNumber("0636656565")
                .withEmailAdress("foo@bar.fr")
                .build();
    }

    public static OneyAddress createDefaultCustomerAdress() {
        return OneyAddress.Builder.aOneyAddressBuilder()
                .withLine1("12 place de la Comedie")
                .withLine2("residence ABC")
                .withLine3("bat D")
                .withLine4("etage E")
                .withLine5("porte F")
                .withCountryCode("FRA")
                .withCountryLabel("France")
                .withPostalCode("34000")
                .withMunicipality("mtp")
                .build();

    }

    public static CustomerIdentity createDefaultCustomerIdentity() {

        return CustomerIdentity.Builder.aCustomerIdentity()
                .withBirthName("Doe")
                .withPersonType(2)
                .withHonorificCode(1)
                .withFirstName("John")
                .build();
    }

    public static PurchaseMerchant createPurchaseMerchant() {
        return PurchaseMerchant.Builder.aPurchaseMerchantBuilder()
                .withCompanyName("cie")
                .withExternalReference("ref")
                .withMerchantGuid("guid")
                .withMunicipality("city")
                .build();
    }

    public static Delivery createDelivery() {

        return Delivery.Builder.aDeliveryBuilder()
                .withDeliveryDate("1998-07-12")
                .withDeliveryModeCode(1)
                .withDeliveryOption(1)
                .withAddressType(1)
                .withRecipient(null)
                .withDeliveryAddress(OneyAddress.Builder.aOneyAddressBuilder()
                        .fromPayline(createDefaultBuyer(), Buyer.AddressType.DELIVERY)
                        .build())
                .build();
    }

    public static List<Item> createItemList() {
        List<Item> itemList = new ArrayList<>();
        itemList.add(Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label")
                .withPrice(110f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build());
        itemList.add(Item.Builder.aItemBuilder()
                .withMainItem(0)
                .withCategoryCode(0)
                .withLabel("label")
                .withPrice(40f)
                .withMarketplaceFlag(0)
                .withQuantity(5)
                .withItemExternalCode("externalCode")
                .build());
        return itemList;

    }

    public static OneyConfirmRequest createOneyConfirmRequest() {
        return null;
    }


    public static OneyTransactionStatusRequest createDefaultTransactionStatusRequest() {
        return OneyTransactionStatusRequest.Builder.aOneyGetStatusRequest()
                .withLanguageCode("FR")
                .withMerchantGuid("9813e3ff-c365-43f2-8dca-94b850befbf9")
                .withPspGuid("6ba2a5e2-df17-4ad7-8406-6a9fc488a60a")
                .withPurchaseReference("CMDE" + OneyConstants.PIPE + "455454545415451198114")
                .build();
    }
}
