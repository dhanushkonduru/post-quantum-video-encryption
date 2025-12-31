package com.pqc.videoencryption.crypto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class KeyDerivation {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyDerivation.class);
    
    private KeyDerivation() {
        throw new AssertionError("Utility class");
    }
    
    public static byte[] deriveKey(byte[] sharedSecret, byte[] salt, byte[] info, int keyLength) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        if (sharedSecret == null || sharedSecret.length == 0) {
            throw new IllegalArgumentException("Shared secret cannot be null or empty");
        }
        if (keyLength < 1 || keyLength > 255 * 32) {
            throw new IllegalArgumentException("Invalid key length");
        }
        
        Mac hmac = Mac.getInstance("HmacSHA256", CryptoConstants.BC_PROVIDER);
        SecretKeySpec hmacKey = new SecretKeySpec(sharedSecret, "HmacSHA256");
        hmac.init(hmacKey);
        
        byte[] prk = extract(hmac, sharedSecret, salt);
        byte[] okm = expand(hmac, prk, info, keyLength);
        
        wipe(prk);
        logger.debug("Derived {} bytes key using HKDF-SHA256", keyLength);
        
        return okm;
    }
    
    private static byte[] extract(Mac hmac, byte[] ikm, byte[] salt) 
            throws InvalidKeyException {
        if (salt == null || salt.length == 0) {
            salt = new byte[32];
            Arrays.fill(salt, (byte) 0);
        }
        hmac.init(new SecretKeySpec(salt, "HmacSHA256"));
        return hmac.doFinal(ikm);
    }
    
    private static byte[] expand(Mac hmac, byte[] prk, byte[] info, int length) 
            throws InvalidKeyException {
        int n = (length + 31) / 32;
        byte[] okm = new byte[length];
        byte[] t = new byte[0];
        
        hmac.init(new SecretKeySpec(prk, "HmacSHA256"));
        
        for (int i = 0; i < n; i++) {
            hmac.update(t);
            if (info != null) {
                hmac.update(info);
            }
            hmac.update((byte) (i + 1));
            t = hmac.doFinal();
            
            int offset = i * 32;
            int copyLength = Math.min(32, length - offset);
            System.arraycopy(t, 0, okm, offset, copyLength);
        }
        
        return okm;
    }
    
    private static void wipe(byte[] data) {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }
}

