package com.payline.payment.oney.service.impl;

import com.payline.pmapi.service.TransactionManagerService;

import java.util.HashMap;
import java.util.Map;

public class TransactionManagerServiceImpl implements TransactionManagerService {

    @Override
    public Map<String, String> readAdditionalData(String s, String s1) {
        // Map vide par default
        return new HashMap<>();
    }
}
