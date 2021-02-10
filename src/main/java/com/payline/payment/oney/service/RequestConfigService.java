package com.payline.payment.oney.service;

import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.pmapi.bean.buyer.request.BuyerDetailsRequest;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.notification.request.NotificationRequest;
import com.payline.pmapi.bean.payment.ContractConfiguration;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.reset.request.ResetRequest;

import java.util.Map;

public interface RequestConfigService {

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request ResetRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(ResetRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request RefundRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(RefundRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request PaymentFormLogoRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(PaymentFormLogoRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request PaymentFormConfigurationRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(PaymentFormConfigurationRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request NotifyTransactionStatusRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(NotifyTransactionStatusRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request PaymentRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(PaymentRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request TransactionStatusRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(TransactionStatusRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request ContractParametersCheckRequest
     * @param key     property key
     * @return teh coreesponding String value
     */
    String getParameterValue(ContractParametersCheckRequest request, String key) throws InvalidDataException;


    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request CaptureRequest
     * @param key     property key
     * @return the corresponding String value
     */
    String getParameterValue(CaptureRequest request, String key) throws InvalidDataException;


    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request BuyerDetailsRequest
     * @param key     property key
     * @return the corresponding String value
     */
    String getParameterValue(BuyerDetailsRequest request, String key) throws InvalidDataException;

    /**
     * Use PARAMETERS_MAP to read a property in ContractConfiguration or in PartnerConfiguration.
     *
     * @param request NotificationRequest
     * @param key     property key
     * @return the corresponding String value
     */
    String getParameterValue(NotificationRequest request, String key) throws InvalidDataException;


    /**
     * @param partnerConfiguration partner Configuration map
     * @param key                  property key
     * @return the corresponding String value
     */
    default String safeGetValue(PartnerConfiguration partnerConfiguration, String key) {

        if (partnerConfiguration == null || key == null || key.isEmpty()) {
            return null;
        }
        return partnerConfiguration.getProperty(key);
    }

    /**
     * @param partnerConfiguration partner Configuration map
     * @param key                  property key
     * @param ext                  country code to build business key
     * @return the corresponding String value
     */
    default String safeGetValue(PartnerConfiguration partnerConfiguration, String key, String ext) throws InvalidDataException {

        if (partnerConfiguration == null || key == null || key.isEmpty()) {
            return null;
        }

        if (ext == null || ext.isEmpty()) {
            throw new InvalidDataException("Extention not found for partner configuration key " + key, key);
        }

        String realKey = key + "." + ext.toLowerCase();
        return partnerConfiguration.getProperty(realKey);
    }

    /**
     * @param contractConfiguration contract Configuration map
     * @param key                   property key
     * @return the corresponding String value
     */
    default String safeGetValue(ContractConfiguration contractConfiguration, String key) {

        if (contractConfiguration == null || key == null || contractConfiguration.getProperty(key) == null) {
            return null;
        }
        return contractConfiguration.getProperty(key).getValue();
    }

    /**
     * @param accountInfo a account info map
     * @param key         property key
     * @return the corresponding String value
     */
    default String safeGetValue(Map<String, String> accountInfo, String key) {

        if (accountInfo == null || key == null) {
            return null;
        }
        return accountInfo.get(key);
    }


}
