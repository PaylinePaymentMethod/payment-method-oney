package com.payline.payment.oney.service.impl;

import com.payline.payment.oney.exception.InvalidDataException;
import com.payline.payment.oney.service.RequestConfigService;
import com.payline.payment.oney.utils.OneyConstants;
import com.payline.pmapi.bean.buyer.request.BuyerDetailsRequest;
import com.payline.pmapi.bean.capture.request.CaptureRequest;
import com.payline.pmapi.bean.configuration.request.ContractParametersCheckRequest;
import com.payline.pmapi.bean.payment.request.NotifyTransactionStatusRequest;
import com.payline.pmapi.bean.payment.request.PaymentRequest;
import com.payline.pmapi.bean.payment.request.TransactionStatusRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormConfigurationRequest;
import com.payline.pmapi.bean.paymentform.request.PaymentFormLogoRequest;
import com.payline.pmapi.bean.refund.request.RefundRequest;
import com.payline.pmapi.bean.reset.request.ResetRequest;

import java.util.HashMap;
import java.util.Map;

public enum RequestConfigServiceImpl implements RequestConfigService {

    INSTANCE;


    private enum PaylineParameterType {
        CONTRACT_CONFIGURATION_PARAMETER,
        PARTNER_CONFIGURATION_PARAMETER,
        EXT_PARTNER_CONFIGURATION_PARAMETER;
    }

    /**
     * Map of all Contract or Partner parameters
     */
    private static final Map<String, PaylineParameterType> PARAMETERS_MAP = new HashMap<>();

    static {
        PARAMETERS_MAP.put(OneyConstants.MERCHANT_GUID_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.OPC_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.NB_ECHEANCES_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.COUNTRY_CODE_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.LANGUAGE_CODE_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.ID_INTERNATIONAL_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.PSP_GUID_KEY, PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.PARTNER_CHIFFREMENT_KEY, PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.PARTNER_AUTHORIZATION_KEY, PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER);
        PARAMETERS_MAP.put(OneyConstants.PARTNER_API_URL, PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER);
    }

    RequestConfigServiceImpl() {
        // ras
    }

    @Override
    public String getParameterValue(ResetRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }


    @Override
    public String getParameterValue(RefundRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(PaymentFormLogoRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(PaymentFormConfigurationRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(NotifyTransactionStatusRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(PaymentRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(TransactionStatusRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(ContractParametersCheckRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            // Dans le cas de la ContractParametersCheckRequest les Contract parameters sont lus dans account Info
            return safeGetValue(request.getAccountInfo(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getAccountInfo(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(CaptureRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

    @Override
    public String getParameterValue(BuyerDetailsRequest request, String key) throws InvalidDataException {
        PaylineParameterType paylineParameterType = PARAMETERS_MAP.get(key);
        if (PaylineParameterType.CONTRACT_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getContractConfiguration(), key);
        } else if (PaylineParameterType.PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            return safeGetValue(request.getPartnerConfiguration(), key);
        } else if (PaylineParameterType.EXT_PARTNER_CONFIGURATION_PARAMETER == paylineParameterType) {
            String ext = safeGetValue(request.getContractConfiguration(), OneyConstants.COUNTRY_CODE_KEY);
            return safeGetValue(request.getPartnerConfiguration(), key, ext);
        }
        return null;
    }

}
