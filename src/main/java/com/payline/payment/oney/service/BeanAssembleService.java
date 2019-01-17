package com.payline.payment.oney.service;

import com.payline.payment.oney.bean.common.NavigationData;
import com.payline.payment.oney.bean.common.customer.Customer;
import com.payline.payment.oney.bean.common.payment.BusinessTransactionData;
import com.payline.payment.oney.bean.common.payment.PaymentData;
import com.payline.payment.oney.bean.common.purchase.Purchase;
import com.payline.payment.oney.exception.InvalidRequestException;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

public interface BeanAssembleService {

    Customer assembleCustomer(PaymentRequest paymentRequest);

    PaymentData assemblePaymentData(PaymentRequest paymentRequest, BusinessTransactionData businessTransaction);

    BusinessTransactionData assembleBuisnessTransactionData(PaymentRequest paymentRequest);

    NavigationData assembleNavigationData(PaymentRequest paymentRequest) throws InvalidRequestException;

    Purchase assemblePurchase(PaymentRequest paymentRequest);
}
