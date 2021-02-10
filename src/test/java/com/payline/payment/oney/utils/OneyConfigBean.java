package com.payline.payment.oney.utils;

import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import com.payline.pmapi.logger.LogManager;
import mockit.Mock;
import mockit.MockUp;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;
import java.util.Properties;

import static com.payline.payment.oney.utils.OneyConstants.CHIFFREMENT_IS_ACTIVE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OneyConfigBean {


    private MockUp<ConfigPropertiesEnum> configPropertiesEnumMockUp;

    private Properties properties;

    @BeforeAll
    public void setup() throws Exception {
        ConfigPropertiesEnum configPropertiesEnum = ConfigPropertiesEnum.INSTANCE;
        Field f = configPropertiesEnum.getClass().getDeclaredField("properties"); //NoSuchFieldException
        f.setAccessible(true);
        Object value = f.get(configPropertiesEnum);
        properties = (Properties) value;
    }

    /**
     * Mock de ConfigPropertiesEnum avec retour de la valeur désirée pour CHIFFREMENT_IS_ACTIVE
     *
     * @param s boolean : chiffrement activé ?
     */
    protected void mockCorrectlyConfigPropertiesEnum(final boolean s) {
        configPropertiesEnumMockUp = new MockUp<ConfigPropertiesEnum>() {

            @Mock
            public String get(String key) {
                if (CHIFFREMENT_IS_ACTIVE.equals(key)) {
                    return String.valueOf(s);
                } else {
                    return ConfigPropertiesEnum.INSTANCE.getProperty(properties, key);
                }
            }
        };
    }

}
