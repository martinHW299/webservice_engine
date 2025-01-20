package com.tigo.api.ws_engine.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;


@Component
public class EncryptionTool {

    @Value("${encryption.algorithm}")
    private String ENCRYPTION_ALGORITHM;

    @Value("${encryption.iteration-count}")
    private int ITERATION_COUNT;

    @Value("${encryption.salt-length}")
    private int SALT_LENGTH;

    public String encrypt(String password, String passphrase) throws Exception {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generateSecret(keySpec), paramSpec);

        byte[] encryptedBytes = cipher.doFinal(password.getBytes());

        byte[] encryptedDataWithSalt = new byte[salt.length + encryptedBytes.length];
        System.arraycopy(salt, 0, encryptedDataWithSalt, 0, salt.length);
        System.arraycopy(encryptedBytes, 0, encryptedDataWithSalt, salt.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(encryptedDataWithSalt);
    }

    public String decrypt(String encryptedPassword, String passphrase) throws Exception {
        byte[] encryptedDataWithSalt = Base64.getDecoder().decode(encryptedPassword);

        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(encryptedDataWithSalt, 0, salt, 0, SALT_LENGTH);

        byte[] encryptedBytes = new byte[encryptedDataWithSalt.length - SALT_LENGTH];
        System.arraycopy(encryptedDataWithSalt, SALT_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(keySpec), paramSpec);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}
