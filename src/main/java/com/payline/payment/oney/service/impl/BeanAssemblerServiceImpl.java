package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.OneyAddress;
import com.payline.payment.oney.bean.common.customer.ContactDetails;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.customer.CustomerIdentity;
import com.payline.payment.oney.bean.common.customer.PurchaseHistory;
import com.payline.payment.oney.bean.common.enums.CategoryCodeHandler;
import com.payline.payment.oney.bean.common.enums.MeanOfTransport;
import com.payline.payment.oney.bean.common.enums.PaymentType;
import com.payline.payment.oney.bean.common.enums.StayType;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.*;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.exception.InvalidFieldFormatException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.common.Amount;
import com.payline.pmapi.bean.common.Buyer;
import com.payline.pmapi.bean.payment.BuyerExtendedHistory;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.travel.*;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import static com.payline.payment.oney.utils.OneyConstants.EXTERNAL_REFERENCE_TYPE;
import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;

/**
 * This class performs all the mapping between Payline data and Oney requests content.
 */
public class BeanAssemblerServiceImpl implements BeanAssembleService {

    private static final Logger LOGGER = LogManager.getLogger(BeanAssemblerServiceImpl.class);

    /**
     * Instantiate a HTTP client with default values.
     */
    private BeanAssemblerServiceImpl() {
    }

    /**
     * @return the singleton instance
     */
    public static BeanAssemblerServiceImpl getInstance() {
        return BeanAssemblerServiceImpl.SingletonHolder.INSTANCE;
    }

    /**
     * Singleton Holder
     */
    private static class SingletonHolder {
        private static final BeanAssemblerServiceImpl INSTANCE = new BeanAssemblerServiceImpl();
    }


    @Override
    public BusinessTransactionData assembleBuisnessTransactionData(final PaymentRequest paymentRequest)
            throws InvalidFieldFormatException {

        return BusinessTransactionData.Builder.aBusinessTransactionDataBuilder()
                .fromPayline(paymentRequest.getContractConfiguration())
                .build();
    }

    @Override
    public Customer assembleCustomer(final PaymentRequest paymentRequest) throws InvalidDataException {
        Buyer buyer = paymentRequest.getBuyer();
        if (buyer == null) {
            return null;
        }

        Customer.Builder customerBuilder = Customer.Builder.aCustomBuilder()
                .withCustomerExternalCode(buyer.getCustomerIdentifier())
                .withLanguageCode(paymentRequest.getLocale().getLanguage())
                .withCustomerIdentity(this.assembleCustomerIdentity(buyer))
                .withContactDetails(ContactDetails.Builder.aContactDetailsBuilder()
                        .fromPayline(buyer)
                        .build()
                )
                .withCustomerAddress(OneyAddress.Builder.aOneyAddressBuilder()
                        .fromPayline(buyer, Buyer.AddressType.BILLING)
                        .build()
                );

        // Trust flag
        if (paymentRequest.getOrder() != null) {
            customerBuilder.withTrustFlag(paymentRequest.getOrder().getRiskLevel());
        }

        return customerBuilder.build();
    }

    @Override
    public CustomerIdentity assembleCustomerIdentity(Buyer buyer) throws InvalidDataException {
        if (buyer == null) {
            return null;
        }
        CustomerIdentity.Builder ciBuilder = CustomerIdentity.Builder.aCustomerIdentity()
                .withTaxpayerCode(buyer.getLegalDocument())
                .withPersonType(PluginUtils.getPersonType(buyer.getLegalStatus()));

        if (buyer.getFullName() != null) {
            Buyer.FullName fullName = buyer.getFullName();
            if (fullName.getCivility() != null) {
                ciBuilder.withHonorificCode(PluginUtils.getHonorificCode(fullName.getCivility()));
            }
            ciBuilder.withBirthName(fullName.getLastName())
                    .withFirstName(fullName.getFirstName())
                    .withLastName(fullName.getLastName());
        }

        if (buyer.getBirthday() != null) {
            ciBuilder.withBirthDate(PluginUtils.dateToString(buyer.getBirthday()));
        }


        return ciBuilder.build();
    }

    @Override
    public Delivery assembleDelivery(PaymentRequest paymentRequest) throws InvalidDataException {
        Delivery.Builder deliveryBuilder = Delivery.Builder.aDeliveryBuilder();

        Order order = paymentRequest.getOrder();
        Integer deliveryModeCode = null;
        if (order != null) {
            if (order.getExpectedDeliveryDate() != null) {
                deliveryBuilder.withDeliveryDate(PluginUtils.dateToString(order.getExpectedDeliveryDate()));
            }

            if (paymentRequest.getOrder() != null) {
                deliveryModeCode = PluginUtils.getOneyDeliveryModeCode(order.getDeliveryMode());
                deliveryBuilder.withDeliveryModeCode(deliveryModeCode);
                deliveryBuilder.withDeliveryOption(PluginUtils.getOneyDeliveryOption(order.getDeliveryTime()));
            }
        }

        // set the address_type from the delivery mode code (see JIRA 175)
        if (deliveryModeCode != null) {
            switch (deliveryModeCode) {
                case 1:
                case 2:
                case 3:
                    deliveryBuilder.withAddressType(deliveryModeCode);
                    break;
                case 4:
                    deliveryBuilder.withAddressType(5);
                    break;
                case 5:
                    deliveryBuilder.withAddressType(6);
                    break;
                case 6:
                    deliveryBuilder.withAddressType(4);
                    break;
                default:
                    deliveryBuilder.withAddressType(1);
                    break;
            }
        }

        Buyer buyer = paymentRequest.getBuyer();
        if (buyer != null) {
            deliveryBuilder.withRecipient(
                    Recipient.Builder.aRecipientBuilder()
                            .fromPayline(buyer)
                            .build()
            );
            deliveryBuilder.withDeliveryAddress(
                    OneyAddress.Builder.aOneyAddressBuilder()
                            .fromPayline(buyer, Buyer.AddressType.DELIVERY)
                            .build()
            );
        }

        return deliveryBuilder.build();
    }

    @Override
    public List<Journey> assembleJourneyList(Transport transport) {
        List<Journey> list = new ArrayList<>();

        if (transport != null && transport.getLegList() != null && !transport.getLegList().isEmpty()) {
            for (Leg leg : transport.getLegList()) {
                list.add(assembleJourney(leg, transport.getTransportMode(), transport.getTravelerList()));
            }
        }

        return list;
    }

    private Journey assembleJourney(Leg leg, TransportMode mode, List<Traveler> travelers) {
        Journey.JourneyBuilder jBuilder = new Journey.JourneyBuilder()
                .withJourneyNumber(leg.getSegment())
                .withJourneyDate(leg.getDepartureDate())
                .withDepartureCity(leg.getDepartureAirport())
                .withArrivalCity(leg.getArrivalAirport())
                .withTicketCategory(leg.getTicketClass());

        if (leg.getIsTicketRestricted() != null) {
            jBuilder.withExchangeabilityFlag("N".equals(leg.getIsTicketRestricted()));
        }
        if (leg.getHasTicketInsurance() != null) {
            jBuilder.withTravelInsuranceFlag("O".equals(leg.getHasTicketInsurance()));
        }

        // Mean of transport
        MeanOfTransport mot = null;
        if (mode != null) {
            try {
                int transportModeInt = Integer.parseInt(mode.getValue());
                mot = MeanOfTransport.fromCode(transportModeInt);
            } catch (NumberFormatException e) {
                LOGGER.error("TransportMode [{}] cannot be cast as integer", mode.getValue());
            }
        }
        if (mot != null) {
            jBuilder.withMeanOfTransport(mot);
        }

        // Number of travelers
        if (travelers != null) {
            jBuilder.withNumberOfTravelers(travelers.size());
        }
        return jBuilder.build();
    }

    @Override
    public NavigationData assembleNavigationData(final PaymentRequest paymentRequest) throws PluginTechnicalException {
        final Environment environment = paymentRequest.getEnvironment();

        if (environment == null) {
            throw new InvalidDataException("Payment Environment must not be null", "PaymentRequest.Environment");
        }

        return NavigationData.Builder.aNavigationDataBuilder()
                .withNotificationUrl(environment.getNotificationURL())
                .withSuccesUrl(environment.getRedirectionReturnURL())
                .withPendingUrl(environment.getRedirectionReturnURL())
                .withFailUrl(environment.getRedirectionReturnURL())
                .build();
    }

    @Override
    public PaymentData assemblePaymentData(
            final PaymentRequest paymentRequest, final BusinessTransactionData businessTransaction) {
        final float amount = createFloatAmount(paymentRequest.getAmount().getAmountInSmallestUnit(),
                paymentRequest.getAmount().getCurrency());
        final String currencyCode = paymentRequest.getAmount().getCurrency().getCurrencyCode();

        return PaymentData.Builder.aPaymentData()
                .withAmount(amount)
                .withCurrency(currencyCode)
                .withPaymentType(PaymentType.IMMEDIATE.getValue())
                .withBusinessTransactionList(businessTransaction)
                .build();

    }

    @Override
    public Purchase assemblePurchase(final PaymentRequest paymentRequest) throws InvalidDataException {
        Purchase.Builder purchaseBuilder = Purchase.Builder.aPurchaseBuilder();
        purchaseBuilder.withExternalReferenceType(EXTERNAL_REFERENCE_TYPE);

        if (paymentRequest != null) {
            Order order = paymentRequest.getOrder();
            if (order != null) {
                purchaseBuilder.withExternalReference(order.getReference());
                Amount amount = order.getAmount();
                if (amount != null && amount.getCurrency() != null) {
                    purchaseBuilder.withPurchaseAmount(createFloatAmount(amount.getAmountInSmallestUnit(), amount.getCurrency()));
                    purchaseBuilder.withCurrencyCode(amount.getCurrency().getCurrencyCode());
                }

            }

            // Delivery
            purchaseBuilder.withDelivery(this.assembleDelivery(paymentRequest));

            // Item list
            int numberOfItem = 0;
            List<Order.OrderItem> orderItems = paymentRequest.getOrder().getItems();
            for (Order.OrderItem item: orderItems) {
                numberOfItem+=item.getQuantity();
            }
            purchaseBuilder.withNumberOfItems(numberOfItem);

            List<Item> listItems = new ArrayList<>();

            // Travel item (same for each item : see PAYLAPMEXT-153).
            Travel travel = null;
            if (order != null && order.getOrderOTA() != null
                    && (order.getOrderOTA().getTransport() != null || order.getOrderOTA().getAccommodation() != null)) {
                travel = this.assembleTravel(order);
            }

            for (Order.OrderItem item : orderItems) {
                // new Item
                Item.Builder itemBuilder = Item.Builder.aItemBuilder()
                        .withMainItem(0)
                        .withCategoryCode(CategoryCodeHandler.findCategory(item.getCategory()))
                        .withLabel(item.getComment()) //or get Brand +" "+ get comment ?
                        .withItemExternalCode(item.getReference())
                        .withQuantity(item.getQuantity().intValue())
                        .withPrice(createFloatAmount(item.getAmount().getAmountInSmallestUnit(), item.getAmount().getCurrency()));

                // Travel
                itemBuilder.withTravel(travel);

                // Marketplace
                if (paymentRequest.getSubMerchant() != null && paymentRequest.getSubMerchant().getSubMerchantName() != null) {
                    itemBuilder.withMarketplaceFlag(1)
                            .withMarketplaceName(paymentRequest.getSubMerchant().getSubMerchantName());
                } else {
                    itemBuilder.withMarketplaceFlag(0);
                }

                listItems.add(itemBuilder.build());
            }

            // add delivery fee as an item if needed (see PAYLAPMEXT-238)
            Amount deliveryCharge = order.getDeliveryCharge();
            if (deliveryCharge != null
                    && deliveryCharge.getAmountInSmallestUnit() != null
                    && !BigInteger.ZERO.equals(deliveryCharge.getAmountInSmallestUnit())) {

                Item item = Item.Builder.aItemBuilder()
                        .withMainItem(0)
                        .withCategoryCode(11)
                        .withLabel("Frais de livraison")
                        .withItemExternalCode("TRANSPORT")
                        .withQuantity(1)
                        .withPrice(createFloatAmount(deliveryCharge.getAmountInSmallestUnit(), deliveryCharge.getCurrency()))
                        .withMarketplaceFlag(0)
                        .build();

                listItems.add(item);

            }

            //Define the main item
            Item.defineMainItem(listItems);
            purchaseBuilder.withListItem(listItems);
        }

        return purchaseBuilder.build();
    }

    @Override
    public PurchaseHistory assemblePurchaseHistory(PaymentRequest paymentRequest) throws InvalidDataException {
        // init variables
        PurchaseHistory.Builder purchaseHistoryBuilder = PurchaseHistory.Builder.aPurchaseHistoryBuilder();

        // checks data is not null
        BuyerExtendedHistory buyerExtendedHistory = paymentRequest.getBuyer().getBuyerExtendedHistory();
        if (buyerExtendedHistory != null) {
            if (buyerExtendedHistory.getFirstOrderDate() != null) {
                purchaseHistoryBuilder.withFirstPurchaseDate(PluginUtils.dateToString(buyerExtendedHistory.getFirstOrderDate()));
            }
            if (buyerExtendedHistory.getLastOrderDate() != null) {
                purchaseHistoryBuilder.withLastPurchaseDate(PluginUtils.dateToString(buyerExtendedHistory.getLastOrderDate()));
            }

            if (buyerExtendedHistory.getTotalAmount() != null && buyerExtendedHistory.getTotalCurrency() != null) {
                purchaseHistoryBuilder.withTotalAmount(
                        PluginUtils.createFloatAmount(
                                BigInteger.valueOf(buyerExtendedHistory.getTotalAmount()),
                                Currency.getInstance(buyerExtendedHistory.getTotalCurrency())
                        )
                );
            }
        }

        return purchaseHistoryBuilder.build();
    }

    @Override
    public List<Stay> assembleStayList(OrderOTA orderOTA) {
        if (orderOTA == null || orderOTA.getAccommodation() == null) {
            return null;
        }
        Accommodation accommodation = orderOTA.getAccommodation();

        Stay.StayBuilder stayBuilder = new Stay.StayBuilder()
                .withArrivalDate(accommodation.getCheckInDate())
                .withDepartureDate(accommodation.getCheckOutDate())
                .withVehicleRentalFlag(orderOTA.getCarRental() != null);

        if (accommodation.getHasInsurance() != null) {
            stayBuilder.withStayInsuranceFlag("O".equals(accommodation.getHasInsurance()));
        }

        // Place of residence
        List<String> placeElements = new ArrayList<>();
        if (accommodation.getName() != null) {
            placeElements.add(accommodation.getName());
        }
        if (accommodation.getCity() != null) {
            placeElements.add(accommodation.getCity());
        }
        if (accommodation.getZipCode() != null) {
            placeElements.add(accommodation.getZipCode());
        }
        if (orderOTA.getCountryDestination() != null) {
            placeElements.add(orderOTA.getCountryDestination());
        }
        String place = PluginUtils.truncate(String.join(" ", placeElements), 64);
        stayBuilder.withPlaceOfResidence(place);

        // Number of travelers
        if (orderOTA.getTransport() != null && orderOTA.getTransport().getTravelerList() != null) {
            stayBuilder.withNumberOfTravelers(orderOTA.getTransport().getTravelerList().size());
        }

        // Stay type
        if (accommodation.getType() != null) {
            if (accommodation.getType() == 0 || accommodation.getType() == 4) {
                stayBuilder.withStayType(StayType.OTHER);
            } else {
                stayBuilder.withStayType(StayType.fromCode(accommodation.getType()));
            }
        }

        List<Stay> list = new ArrayList<>();
        list.add(stayBuilder.build());
        return list;
    }

    @Override
    public Travel assembleTravel(Order order) {
        if (order.getOrderOTA() == null) {
            return null;
        }

        Travel.TravelBuilder travelBuilder = new Travel.TravelBuilder();

        OrderOTA orderOTA = order.getOrderOTA();
        Transport transport = orderOTA.getTransport();

        // Main traveler info
        if (transport != null
                && !transport.getTravelerList().isEmpty()
                && transport.getTravelerList().get(0) != null) {
            Traveler mainTraveler = transport.getTravelerList().get(0);
            travelBuilder.withMainTravelerFirstname(mainTraveler.getFirstName())
                    .withMainTravelerSurname(mainTraveler.getLastName())
                    .withMainTravelerBirthdate(mainTraveler.getBirthDate());
        }

        // Journey
        travelBuilder.withJourney(this.assembleJourneyList(transport));

        // Stay
        travelBuilder.withStay(this.assembleStayList(orderOTA));

        return travelBuilder.build();
    }

}
