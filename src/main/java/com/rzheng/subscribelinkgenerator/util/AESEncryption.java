package com.rzheng.subscribelinkgenerator.util;

import org.springframework.context.annotation.Bean;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Richard
 */
public class AESEncryption {

    private static final String AES = "AES";

    /**
     * Secret key for encryption and decryption.
     * JQWiPLmfCZ7#jbMh
     */
    private static final byte[] KEY_VALUE = new byte[]{'J', 'Q', 'W', 'i', 'P', 'L', 'm', 'f', 'C', 'Z', '7', '#', 'j', 'b', 'M', 'h'};

    @Bean
    public static String encrypt(String plainText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY_VALUE, AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByteValue = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedByteValue);
    }

    @Bean
    public static String decrypt(String encryptedText) throws Exception {
        SecretKeySpec key = new SecretKeySpec(KEY_VALUE, AES);
        Cipher cipher = Cipher.getInstance(AES);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedValue64 = Base64.getDecoder().decode(encryptedText);
        byte[] decryptedByteValue = cipher.doFinal(decryptedValue64);
        return new String(decryptedByteValue, StandardCharsets.UTF_8);
    }
}
