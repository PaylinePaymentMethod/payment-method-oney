package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.customer.PurchaseHistory;
import com.payline.payment.oney.bean.common.enums.PaymentType;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.exception.InvalidFieldFormatException;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.payment.oney.service.BeanAssembleService;
import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.payment.BuyerExtendedHistory;
import com.payline.pmapi.bean.payment.Environment;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;

public class BeanAssemblerServiceImpl implements BeanAssembleService {

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


    @Override
    public Customer assembleCustomer(final PaymentRequest paymentRequest) {
        return Customer.Builder.aCustomBuilder()
                .fromPaylineRequest(paymentRequest)
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
                //v2 mapper payment type (quel champ de la requete)
                .withPaymentType(PaymentType.IMMEDIATE.getValue())
                .withBusinessTransactionList(businessTransaction)
                .build();

    }

    @Override
    public BusinessTransactionData assembleBuisnessTransactionData(final PaymentRequest paymentRequest)
            throws InvalidFieldFormatException {

        return BusinessTransactionData.Builder.aBusinessTransactionDataBuilder()
                .fromPayline(paymentRequest.getContractConfiguration())
                .build();
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
    public Purchase assemblePurchase(final PaymentRequest paymentRequest) {
        return Purchase.Builder.aPurchaseBuilder()
                .fromPayline(paymentRequest)
                .build();
    }

    @Override
    public PurchaseHistory assemblePurchaseHistory(PaymentRequest paymentRequest) {
        // init variables
        PurchaseHistory.Builder purchaseHistoryBuilder = PurchaseHistory.Builder.aPurchaseHistoryBuilder();

        // checks data is not null
        BuyerExtendedHistory buyerExtendedHistory = paymentRequest.getBuyer().getBuyerExtendedHistory();
        if (buyerExtendedHistory != null) {
            if (buyerExtendedHistory.getOrderCount6Months() != null)
                purchaseHistoryBuilder.withNumberOfPurchase(buyerExtendedHistory.getOrderCount6Months());
            if (buyerExtendedHistory.getFirstOrderDate() != null)
                purchaseHistoryBuilder.withFirstPurchasedate(PluginUtils.dateToString(buyerExtendedHistory.getFirstOrderDate()));
            if (buyerExtendedHistory.getLastOrderDate() != null)
                purchaseHistoryBuilder.withLastPurchaseDate(PluginUtils.dateToString(buyerExtendedHistory.getLastOrderDate()));
        }

        if (paymentRequest.getBuyer().getAccountAverageAmount() != null && paymentRequest.getBuyer().getAccountOrderCount() != null) {
            purchaseHistoryBuilder.withTotalAmount(paymentRequest.getBuyer().getAccountAverageAmount().getAmountInSmallestUnit().multiply(paymentRequest.getBuyer().getAccountOrderCount()).floatValue());
        }

        return purchaseHistoryBuilder.build();
    }

    /**
     * Singleton Holder
     */
    private static class SingletonHolder {
        private static final BeanAssemblerServiceImpl INSTANCE = new BeanAssemblerServiceImpl();
    }

}
