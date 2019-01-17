package com.payline.payment.oney.utils.chiffrement;

import com.payline.payment.oney.exception.DecryptException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OneyCrypto {

    private static final Logger LOGGER = LogManager.getLogger(OneyCrypto.class);

    //Symetric key, used to encrypt & decrypt message
    private String key;


    public OneyCrypto(String key) {
        this.key = key;
    }


    /**
     * decrypt message with symetric key
     *
     * @param messageToEncrypt String, message to encrypt
     * @return String, the decrypted message
     */
    public String encrypt(String messageToEncrypt) throws DecryptException {
        // Convert the key to SecretKeySpec
        byte[] decodedKey = DatatypeConverter.parseBase64Binary(this.key);
        SecretKeySpec oneySecret = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        try { // Initialise a cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, oneySecret);
            // Make the encryption
            byte[] decryptedBytes = messageToEncrypt.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(decryptedBytes);
            return DatatypeConverter.printBase64Binary(encryptedBytes);

        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException |
                NoSuchPaddingException | InvalidKeyException e) {
            LOGGER.error("Unable to encrypt this message", e);
            throw new DecryptException("Unable to encrypt this message", e);
        }
    }


    /**
     * decrypt message with symetric key
     *
     * @param messageEncrypted String, message to decrypt
     * @return String, the decrypted message
     */
    public String decrypt(String messageEncrypted) throws DecryptException {
        // Convert the key to SecretKeySpec
        byte[] decodedKey = DatatypeConverter.parseBase64Binary(this.key);
        SecretKeySpec oneySecret = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // Define variables
        byte[] encryptedMessage = DatatypeConverter.parseBase64Binary(messageEncrypted);

        try {
            // Initialise a cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, oneySecret); // Make the decryption
            byte[] decryptedBytes = cipher.doFinal(encryptedMessage);
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException |
                NoSuchPaddingException | InvalidKeyException e) {
            LOGGER.error("Unable to decrypt this message", e);
            throw new DecryptException("Unable to decrypt this message", e);
        }
    }
}
