package com.payline.payment.oney.utils;

import com.payline.payment.oney.exception.DecryptException;
import com.payline.payment.oney.utils.chiffrement.OneyCrypto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OneyCryptoTest {

    private OneyCrypto crypto;


    @Test
    public void encryptOK() throws DecryptException {
        crypto = new OneyCrypto("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=");

        String message = "mon message a chiffrer";
        String messageEncrypted = crypto.encrypt(message);

        Assertions.assertNotEquals(message, messageEncrypted);
        Assertions.assertEquals("w7wNbi0SYzTRnHmVN3tUUiSveqAUZVyttsUtRbXw8Mk=", messageEncrypted);
    }


    @Test
    public void decryptOK() throws DecryptException {
        crypto = new OneyCrypto("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=");

        String messageEncrypted = "SE7ZT07NBY+cOL7B3/jLRdfFBH96VX2mMyxioBE7F8o=";
        String message = crypto.decrypt(messageEncrypted);

        Assertions.assertNotEquals(message, messageEncrypted);
        Assertions.assertEquals("mon message a dechiffrer", message);
    }


    @Test
    public void testEncryptDecrypt() throws DecryptException {
        crypto = new OneyCrypto("66s581CG5W+RLEqZHAGQx+vskjy660Kt8x8rhtRpXtY=");

        String message = "test encrypt/decrypt {somekey : some_value}";
        String messageEncrypted = crypto.encrypt(message);
        String messageDecrypted = crypto.decrypt(messageEncrypted);


        Assertions.assertNotEquals(message, messageEncrypted);
        Assertions.assertNotEquals(messageEncrypted, messageDecrypted);
        Assertions.assertEquals(message, messageDecrypted);

    }

    @Test
    public void encryptWrongKey() {

        Throwable exception = Assertions.assertThrows(DecryptException.class, () -> {
            crypto = new OneyCrypto("maCle");

            String message = "mon message a chiffrer";
            crypto.encrypt(message);

        });

    }


}
