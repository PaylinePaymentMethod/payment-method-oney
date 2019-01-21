package com.payline.payment.oney.utils.properties.service;

import com.payline.payment.oney.utils.config.ConfigEnvironment;

import java.util.Properties;

/**
 * Utility class which reads and provides config properties.
 */
public enum ConfigPropertiesEnum implements PropertiesService {

    INSTANCE;

    private static final String FILENAME = "config.properties";

    private final Properties properties;

    /* This class has only static methods: no need to instantiate it */
    ConfigPropertiesEnum() {
        properties = new Properties();
        // init of the Properties
        readProperties(properties);
    }


    @Override
    public String getFilename() {
        return FILENAME;
    }

    @Override
    public String get(String key) {
        return getProperty(properties, key);
    }

    @Override
    public String get(String key, ConfigEnvironment environment) {
        return getProperty(properties, key, environment);
    }
}
