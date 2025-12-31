package com.pqc.videoencryption.crypto;

import com.pqc.videoencryption.storage.KeyStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public final class VideoEncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(VideoEncryptionService.class);
    
    private VideoEncryptionService() {
        throw new AssertionError("Utility class");
    }
    
    public static void encryptVideo(Path inputVideo, Path outputFile, String username, String password) 
            throws Exception {
        
        Path keystorePath = KeyStoreManager.getKeystorePath(username);
        if (!Files.exists(keystorePath)) {
            KeyStoreManager.createKeyStore(keystorePath, password, username);
        }
        
        SecretKey aesKey = KeyStoreManager.loadAESKey(keystorePath, password);
        
        byte[] videoData = Files.readAllBytes(inputVideo);
        SymmetricEncryption.EncryptionResult result = SymmetricEncryption.encrypt(aesKey, videoData);
        
        try {
            Path parent = outputFile.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            EncryptedVideoFile encryptedFile = new EncryptedVideoFile(
                result.getCiphertext(),
                result.getIv(),
                inputVideo.getFileName().toString()
            );
            
            Files.write(outputFile, encryptedFile.serialize());
            logger.info("Encrypted video: {} -> {}", inputVideo.getFileName(), outputFile.getFileName());
        } finally {
            result.wipe();
            Arrays.fill(videoData, (byte) 0);
            SymmetricEncryption.wipe(aesKey);
        }
    }
    
    public static void decryptVideo(Path inputFile, Path outputVideo, String username, String password) 
            throws Exception {
        
        Path keystorePath = KeyStoreManager.getKeystorePath(username);
        SecretKey aesKey = KeyStoreManager.loadAESKey(keystorePath, password);
        
        byte[] encryptedData = Files.readAllBytes(inputFile);
        EncryptedVideoFile encryptedFile = EncryptedVideoFile.deserialize(encryptedData);
        
        try {
            byte[] decryptedData = SymmetricEncryption.decrypt(
                aesKey, 
                encryptedFile.ciphertext, 
                encryptedFile.iv
            );
            
            Files.write(outputVideo, decryptedData);
            logger.info("Decrypted video: {} -> {}", inputFile.getFileName(), outputVideo.getFileName());
            
            Arrays.fill(decryptedData, (byte) 0);
        } finally {
            SymmetricEncryption.wipe(aesKey);
        }
    }
    
    private static class EncryptedVideoFile {
        final byte[] ciphertext;
        final byte[] iv;
        final String originalFilename;
        
        EncryptedVideoFile(byte[] ciphertext, byte[] iv, String originalFilename) {
            this.ciphertext = ciphertext;
            this.iv = iv;
            this.originalFilename = originalFilename;
        }
        
        byte[] serialize() {
            int filenameLength = originalFilename.getBytes().length;
            int totalLength = 4 + filenameLength + 4 + iv.length + 4 + ciphertext.length;
            byte[] serialized = new byte[totalLength];
            int offset = 0;
            
            putInt(serialized, offset, filenameLength);
            offset += 4;
            System.arraycopy(originalFilename.getBytes(), 0, serialized, offset, filenameLength);
            offset += filenameLength;
            
            putInt(serialized, offset, iv.length);
            offset += 4;
            System.arraycopy(iv, 0, serialized, offset, iv.length);
            offset += iv.length;
            
            putInt(serialized, offset, ciphertext.length);
            offset += 4;
            System.arraycopy(ciphertext, 0, serialized, offset, ciphertext.length);
            
            return serialized;
        }
        
        static EncryptedVideoFile deserialize(byte[] data) {
            int offset = 0;
            
            int filenameLength = getInt(data, offset);
            offset += 4;
            String filename = new String(data, offset, filenameLength);
            offset += filenameLength;
            
            int ivLength = getInt(data, offset);
            offset += 4;
            byte[] iv = new byte[ivLength];
            System.arraycopy(data, offset, iv, 0, ivLength);
            offset += ivLength;
            
            int ciphertextLength = getInt(data, offset);
            offset += 4;
            byte[] ciphertext = new byte[ciphertextLength];
            System.arraycopy(data, offset, ciphertext, 0, ciphertextLength);
            
            return new EncryptedVideoFile(ciphertext, iv, filename);
        }
        
        private static void putInt(byte[] array, int offset, int value) {
            array[offset] = (byte) (value >>> 24);
            array[offset + 1] = (byte) (value >>> 16);
            array[offset + 2] = (byte) (value >>> 8);
            array[offset + 3] = (byte) value;
        }
        
        private static int getInt(byte[] array, int offset) {
            return ((array[offset] & 0xFF) << 24) |
                   ((array[offset + 1] & 0xFF) << 16) |
                   ((array[offset + 2] & 0xFF) << 8) |
                   (array[offset + 3] & 0xFF);
        }
    }
}

