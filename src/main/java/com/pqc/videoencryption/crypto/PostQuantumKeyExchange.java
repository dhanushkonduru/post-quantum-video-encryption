package com.pqc.videoencryption.crypto;

import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public final class PostQuantumKeyExchange {
    
    private static final Logger logger = LoggerFactory.getLogger(PostQuantumKeyExchange.class);
    
    private PostQuantumKeyExchange() {
        throw new AssertionError("Utility class");
    }
    
    public static KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
            CryptoConstants.KYBER_ALGORITHM, 
            CryptoConstants.PQC_PROVIDER
        );
        kpg.initialize(KyberParameterSpec.kyber1024, new SecureRandom());
        KeyPair kp = kpg.generateKeyPair();
        logger.debug("Generated Kyber-1024 key pair");
        return kp;
    }
    
    public static byte[] encapsulate(PublicKey recipientPublicKey) throws GeneralSecurityException {
        Cipher kem = Cipher.getInstance(
            CryptoConstants.KYBER_ALGORITHM, 
            CryptoConstants.PQC_PROVIDER
        );
        kem.init(Cipher.ENCRYPT_MODE, recipientPublicKey, new SecureRandom());
        byte[] sharedSecret = kem.doFinal();
        logger.debug("Kyber encapsulation completed, shared secret: {} bytes", sharedSecret.length);
        return sharedSecret;
    }
    
    public static byte[] decapsulate(PrivateKey privateKey, byte[] encapsulatedKey) 
            throws GeneralSecurityException {
        Cipher kem = Cipher.getInstance(
            CryptoConstants.KYBER_ALGORITHM, 
            CryptoConstants.PQC_PROVIDER
        );
        kem.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] sharedSecret = kem.doFinal(encapsulatedKey);
        logger.debug("Kyber decapsulation completed, shared secret: {} bytes", sharedSecret.length);
        return sharedSecret;
    }
    
    public static PublicKey decodePublicKey(byte[] encoded) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance(
            CryptoConstants.KYBER_ALGORITHM, 
            CryptoConstants.PQC_PROVIDER
        );
        return kf.generatePublic(new X509EncodedKeySpec(encoded));
    }
    
    public static byte[] encodePublicKey(PublicKey publicKey) {
        return publicKey.getEncoded();
    }
    
    public static void wipe(byte[] data) {
        if (data != null) {
            Arrays.fill(data, (byte) 0);
        }
    }
}

