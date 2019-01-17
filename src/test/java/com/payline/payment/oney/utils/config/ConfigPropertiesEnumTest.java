package com.payline.payment.oney.utils.config;

import com.payline.payment.oney.utils.properties.service.ConfigPropertiesEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigPropertiesEnumTest {

    private String key;


    @Test
    public void getFromKeyKO() {
        key = ConfigPropertiesEnum.INSTANCE.get("BadKey");
        Assertions.assertNull(key);

    }

    @Test
    public void getFromKeyOK() {
        key = ConfigPropertiesEnum.INSTANCE.get("http.connectTimeout");
        Assertions.assertNotNull(key);
    }

}
