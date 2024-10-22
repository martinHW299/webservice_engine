package com.boctool.webservice_engine.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;


@Component
public class EncryptionUtils {

/*
    private final static String ENCRYPTION_ALGORITHM = "PBEWithMD5AndDES";
    private final static int ITERATION_COUNT = 10000;// Iteration count for key derivation
    private final static int SALT_LENGTH = 8;// Salt length in bytes

    encryption.algorithm=PBEWithMD5AndDES
encryption.iteration-count=10000
encryption.salt-length=8
*/

    @Value("${encryption.algorithm}")
    private String ENCRYPTION_ALGORITHM;

    @Value("${encryption.iteration-count}")
    private int ITERATION_COUNT;

    @Value("${encryption.salt-length}")
    private int SALT_LENGTH;

    public String encrypt(String password, String passphrase) throws Exception {
        // Generate a random salt
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);

        // Set up the PBE key and cipher
        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generateSecret(keySpec), paramSpec);

        // Encrypt the password
        byte[] encryptedBytes = cipher.doFinal(password.getBytes());

        // Combine salt and encrypted password
        byte[] encryptedDataWithSalt = new byte[salt.length + encryptedBytes.length];
        System.arraycopy(salt, 0, encryptedDataWithSalt, 0, salt.length);
        System.arraycopy(encryptedBytes, 0, encryptedDataWithSalt, salt.length, encryptedBytes.length);

        // Return base64-encoded string
        return Base64.getEncoder().encodeToString(encryptedDataWithSalt);
    }

    // Decrypt the sourcePassword using Password-Based Encryption (PBE)
    public String decrypt(String encryptedPassword, String passphrase) throws Exception {
        byte[] encryptedDataWithSalt = Base64.getDecoder().decode(encryptedPassword);

        // Extract the salt from the encrypted data
        byte[] salt = new byte[SALT_LENGTH];
        System.arraycopy(encryptedDataWithSalt, 0, salt, 0, SALT_LENGTH);

        // Extract the encrypted bytes
        byte[] encryptedBytes = new byte[encryptedDataWithSalt.length - SALT_LENGTH];
        System.arraycopy(encryptedDataWithSalt, SALT_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        // Set up the PBE key and cipher
        PBEKeySpec keySpec = new PBEKeySpec(passphrase.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);

        PBEParameterSpec paramSpec = new PBEParameterSpec(salt, ITERATION_COUNT);
        cipher.init(Cipher.DECRYPT_MODE, keyFactory.generateSecret(keySpec), paramSpec);

        // Decrypt the password
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes);
    }
}
