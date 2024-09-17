package com.boctool.webservice_engine.utils;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtils {
    // PBE parameters
    private static final String ENCRYPTION_ALGORITHM = "PBEWithMD5AndDES"; // PBE Algorithm
    private static final int ITERATION_COUNT = 10000; // Iteration count for key derivation
    private static final int SALT_LENGTH = 8; // Salt length in bytes

    // Encrypt the sourcePassword using Password-Based Encryption (PBE)
    public static String encrypt(String password, String passphrase) throws Exception {
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
    public static String decrypt(String encryptedPassword, String passphrase) throws Exception {
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
