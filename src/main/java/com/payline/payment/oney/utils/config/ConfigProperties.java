package com.payline.payment.oney.utils.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class which reads and provides config properties.
 */
public class ConfigProperties {

    private static final Logger LOGGER = LogManager.getLogger(ConfigProperties.class);

    private static final String FILENAME = "config.properties";

    private static Properties properties;

    /* This class has only static methods: no need to instantiate it */
    private ConfigProperties() {
    }

    /**
     * Get a config property by its name.
     * Warning, if the property is environment-dependent, use {@link ConfigProperties#get(String, ConfigEnvironment)} instead.
     *
     * @param key The name of the property to recover
     * @return The property value. Can be null if the property has not been found.
     */
    public static String get(String key) {
        if (properties == null) {
            LOGGER.error("Property " + key + " doesn't exist");
            readProperties();

        }
        return properties.getProperty(key);
    }

    /**
     * Get a environment-dependent config property by its name.
     *
     * @param key         The name of the property to recover
     * @param environment The runtime environment
     * @return The property value. Can be null if the property has not been found.
     */
    public static String get(String key, ConfigEnvironment environment) {
        String prefix = "";
        if (environment != null) {
            prefix += environment.getPrefix() + ".";
        }
        return get(prefix + key);
    }

    /**
     * Reads the properties file and stores the result.
     */
    private static void readProperties() {

        properties = new Properties();

        try {

            InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream(FILENAME);
            properties.load(inputStream);

        } catch (Exception e) {
            LOGGER.error("An error occurred reading the configuration properties file {}", e.getMessage(), e);
        }

    }

}
