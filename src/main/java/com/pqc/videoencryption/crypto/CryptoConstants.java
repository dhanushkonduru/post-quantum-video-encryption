package com.pqc.videoencryption.crypto;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;

import java.security.Provider;

public final class CryptoConstants {
    
    private CryptoConstants() {
        throw new AssertionError("Utility class");
    }

    public static final Provider BC_PROVIDER = new BouncyCastleProvider();
    public static final Provider PQC_PROVIDER = new BouncyCastlePQCProvider();
    
    public static final String KYBER_ALGORITHM = "KYBER1024";
    public static final String AES_ALGORITHM = "AES";
    public static final String AES_CIPHER = "AES/GCM/NoPadding";
    public static final int AES_KEY_SIZE = 256;
    public static final int GCM_IV_SIZE = 12;
    public static final int GCM_TAG_SIZE = 128;
    
    public static final String HKDF_ALGORITHM = "HKDF";
    public static final String HKDF_HASH = "SHA-256";
    public static final int HKDF_KEY_SIZE = 32;
    
    public static final String KEYSTORE_TYPE = "PKCS12";
    public static final String KEYSTORE_ALIAS_KEM = "Kyber-KeyPair";
    public static final String KEYSTORE_ALIAS_SYMMETRIC = "AES-Key";
    
    static {
        java.security.Security.addProvider(BC_PROVIDER);
        java.security.Security.addProvider(PQC_PROVIDER);
    }
}

