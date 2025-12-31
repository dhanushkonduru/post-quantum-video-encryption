package com.pqc.videoencryption.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import javax.crypto.SecretKey;
import java.security.GeneralSecurityException;

public class CryptoTest {
    
    @Test
    public void testAESEncryptionDecryption() throws Exception {
        byte[] keyMaterial = new byte[32];
        new java.security.SecureRandom().nextBytes(keyMaterial);
        SecretKey key = SymmetricEncryption.createKey(keyMaterial);
        
        byte[] plaintext = "Test video data".getBytes();
        SymmetricEncryption.EncryptionResult result = SymmetricEncryption.encrypt(key, plaintext);
        
        assertNotNull(result.getCiphertext());
        assertNotNull(result.getIv());
        assertEquals(12, result.getIv().length);
        
        byte[] decrypted = SymmetricEncryption.decrypt(key, result.getCiphertext(), result.getIv());
        assertArrayEquals(plaintext, decrypted);
        
        result.wipe();
        SymmetricEncryption.wipe(key);
    }
    
    @Test
    public void testKyberKeyGeneration() throws GeneralSecurityException {
        var keyPair = PostQuantumKeyExchange.generateKeyPair();
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }
    
    @Test
    public void testKeyDerivation() throws Exception {
        byte[] sharedSecret = new byte[32];
        new java.security.SecureRandom().nextBytes(sharedSecret);
        byte[] salt = new byte[32];
        new java.security.SecureRandom().nextBytes(salt);
        byte[] info = "test-context".getBytes();
        
        byte[] derivedKey = KeyDerivation.deriveKey(sharedSecret, salt, info, 32);
        assertEquals(32, derivedKey.length);
        
        byte[] derivedKey2 = KeyDerivation.deriveKey(sharedSecret, salt, info, 32);
        assertArrayEquals(derivedKey, derivedKey2);
    }
    
    @Test
    public void testIVUniqueness() throws Exception {
        byte[] keyMaterial = new byte[32];
        new java.security.SecureRandom().nextBytes(keyMaterial);
        SecretKey key = SymmetricEncryption.createKey(keyMaterial);
        
        byte[] plaintext = "Test".getBytes();
        SymmetricEncryption.EncryptionResult result1 = SymmetricEncryption.encrypt(key, plaintext);
        SymmetricEncryption.EncryptionResult result2 = SymmetricEncryption.encrypt(key, plaintext);
        
        assertFalse(java.util.Arrays.equals(result1.getIv(), result2.getIv()));
        
        result1.wipe();
        result2.wipe();
        SymmetricEncryption.wipe(key);
    }
}

