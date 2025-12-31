package com.pqc.videoencryption.storage;

import com.pqc.videoencryption.crypto.CryptoConstants;
import com.pqc.videoencryption.crypto.PostQuantumKeyExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.Arrays;

public final class KeyStoreManager {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyStoreManager.class);
    
    private KeyStoreManager() {
        throw new AssertionError("Utility class");
    }
    
    public static void createKeyStore(Path keystorePath, String password, String username) 
            throws GeneralSecurityException, IOException {
        
        KeyStore keyStore = KeyStore.getInstance(CryptoConstants.KEYSTORE_TYPE);
        keyStore.load(null, null);
        
        SecretKey aesKey = generateAESKey();
        keyStore.setEntry(
            CryptoConstants.KEYSTORE_ALIAS_SYMMETRIC,
            new KeyStore.SecretKeyEntry(aesKey),
            new KeyStore.PasswordProtection(password.toCharArray())
        );
        
        Path parent = keystorePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        try (OutputStream os = Files.newOutputStream(keystorePath)) {
            keyStore.store(os, password.toCharArray());
        }
        
        logger.info("Created keystore for user: {}", username);
        wipeKey(aesKey);
    }
    
    public static KeyPair loadOrGenerateKeyPair(Path keystorePath, String password) 
            throws GeneralSecurityException, IOException {
        
        Path publicKeyFile = keystorePath.resolveSibling(
            keystorePath.getFileName().toString().replace(".p12", ".pub")
        );
        
        KeyStore keyStore = KeyStore.getInstance(CryptoConstants.KEYSTORE_TYPE);
        try (InputStream is = Files.newInputStream(keystorePath)) {
            keyStore.load(is, password.toCharArray());
        }
        
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(
            CryptoConstants.KEYSTORE_ALIAS_KEM, 
            password.toCharArray()
        );
        
        PublicKey publicKey;
        if (privateKey == null || !Files.exists(publicKeyFile)) {
            KeyPair keyPair = PostQuantumKeyExchange.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
            
            keyStore.setKeyEntry(
                CryptoConstants.KEYSTORE_ALIAS_KEM,
                privateKey,
                password.toCharArray(),
                null
            );
            
            try (OutputStream os = Files.newOutputStream(keystorePath)) {
                keyStore.store(os, password.toCharArray());
            }
            
            Files.write(publicKeyFile, PostQuantumKeyExchange.encodePublicKey(publicKey));
            logger.debug("Generated and stored new Kyber key pair");
        } else {
            byte[] publicKeyBytes = Files.readAllBytes(publicKeyFile);
            publicKey = PostQuantumKeyExchange.decodePublicKey(publicKeyBytes);
            logger.debug("Loaded existing Kyber key pair");
        }
        
        return new KeyPair(publicKey, privateKey);
    }
    
    public static SecretKey loadAESKey(Path keystorePath, String password) 
            throws GeneralSecurityException, IOException {
        
        KeyStore keyStore = KeyStore.getInstance(CryptoConstants.KEYSTORE_TYPE);
        try (InputStream is = Files.newInputStream(keystorePath)) {
            keyStore.load(is, password.toCharArray());
        }
        
        SecretKey key = (SecretKey) keyStore.getKey(
            CryptoConstants.KEYSTORE_ALIAS_SYMMETRIC, 
            password.toCharArray()
        );
        
        logger.debug("Loaded AES key from keystore");
        return key;
    }
    
    private static SecretKey generateAESKey() throws GeneralSecurityException {
        KeyGenerator kg = KeyGenerator.getInstance(
            CryptoConstants.AES_ALGORITHM, 
            CryptoConstants.BC_PROVIDER
        );
        kg.init(CryptoConstants.AES_KEY_SIZE, new SecureRandom());
        return kg.generateKey();
    }
    
    private static void wipeKey(SecretKey key) {
        if (key instanceof SecretKeySpec) {
            byte[] encoded = key.getEncoded();
            if (encoded != null) {
                Arrays.fill(encoded, (byte) 0);
            }
        }
    }
    
    public static Path getKeystorePath(String username) {
        return Paths.get("keys", username + ".p12");
    }
}
