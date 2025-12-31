package com.pqc.videoencryption.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;

public final class SymmetricEncryption {
    
    private static final Logger logger = LoggerFactory.getLogger(SymmetricEncryption.class);
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private SymmetricEncryption() {
        throw new AssertionError("Utility class");
    }
    
    public static SecretKey createKey(byte[] keyMaterial) {
        if (keyMaterial.length != CryptoConstants.AES_KEY_SIZE / 8) {
            throw new IllegalArgumentException("Key material must be 32 bytes for AES-256");
        }
        return new SecretKeySpec(keyMaterial, CryptoConstants.AES_ALGORITHM);
    }
    
    public static EncryptionResult encrypt(SecretKey key, byte[] plaintext) 
            throws Exception {
        
        if (key == null || plaintext == null) {
            throw new IllegalArgumentException("Key and plaintext cannot be null");
        }
        
        byte[] iv = new byte[CryptoConstants.GCM_IV_SIZE];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance(
            CryptoConstants.AES_CIPHER, 
            CryptoConstants.BC_PROVIDER
        );
        GCMParameterSpec gcmSpec = new GCMParameterSpec(
            CryptoConstants.GCM_TAG_SIZE, 
            iv
        );
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        
        byte[] ciphertext = cipher.doFinal(plaintext);
        
        logger.debug("Encrypted {} bytes plaintext to {} bytes ciphertext", 
            plaintext.length, ciphertext.length);
        
        return new EncryptionResult(ciphertext, iv);
    }
    
    public static byte[] decrypt(SecretKey key, byte[] ciphertext, byte[] iv) 
            throws Exception {
        
        if (key == null || ciphertext == null || iv == null) {
            throw new IllegalArgumentException("Key, ciphertext, and IV cannot be null");
        }
        if (iv.length != CryptoConstants.GCM_IV_SIZE) {
            throw new IllegalArgumentException("IV must be 12 bytes for GCM");
        }
        
        Cipher cipher = Cipher.getInstance(
            CryptoConstants.AES_CIPHER, 
            CryptoConstants.BC_PROVIDER
        );
        GCMParameterSpec gcmSpec = new GCMParameterSpec(
            CryptoConstants.GCM_TAG_SIZE, 
            iv
        );
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        
        byte[] plaintext = cipher.doFinal(ciphertext);
        
        logger.debug("Decrypted {} bytes ciphertext to {} bytes plaintext", 
            ciphertext.length, plaintext.length);
        
        return plaintext;
    }
    
    public static void wipe(SecretKey key) {
        if (key instanceof SecretKeySpec) {
            byte[] encoded = key.getEncoded();
            if (encoded != null) {
                Arrays.fill(encoded, (byte) 0);
            }
        }
    }
    
    public static final class EncryptionResult {
        private final byte[] ciphertext;
        private final byte[] iv;
        
        public EncryptionResult(byte[] ciphertext, byte[] iv) {
            this.ciphertext = ciphertext;
            this.iv = iv;
        }
        
        public byte[] getCiphertext() {
            return ciphertext;
        }
        
        public byte[] getIv() {
            return iv;
        }
        
        public void wipe() {
            if (ciphertext != null) {
                Arrays.fill(ciphertext, (byte) 0);
            }
            if (iv != null) {
                Arrays.fill(iv, (byte) 0);
            }
        }
    }
}

