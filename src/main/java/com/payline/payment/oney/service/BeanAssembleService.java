package com.payline.payment.oney.service;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.customer.PurchaseHistory;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.exception.PluginTechnicalException;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

public interface BeanAssembleService {

    Customer assembleCustomer(PaymentRequest paymentRequest) throws PluginTechnicalException;

    PaymentData assemblePaymentData(PaymentRequest paymentRequest, BusinessTransactionData businessTransaction) throws PluginTechnicalException;

    BusinessTransactionData assembleBuisnessTransactionData(PaymentRequest paymentRequest) throws PluginTechnicalException;

    NavigationData assembleNavigationData(PaymentRequest paymentRequest) throws PluginTechnicalException;

    Purchase assemblePurchase(PaymentRequest paymentRequest) throws PluginTechnicalException;

    PurchaseHistory assemblePurchaseHistory(PaymentRequest paymentRequest);
}
