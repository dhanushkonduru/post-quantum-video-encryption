package com.pqc.videoencryption.storage;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

public final class UserRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private static final Path USERS_FILE = Paths.get("data", "users.txt");
    private static final int BCRYPT_ROUNDS = 12;
    
    private UserRepository() {
        throw new AssertionError("Utility class");
    }
    
    public static void initialize() throws IOException {
        Path parent = USERS_FILE.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.exists(USERS_FILE)) {
            Files.createFile(USERS_FILE);
        }
    }
    
    public static void createUser(String username, String password, String contact, 
                                   String email, String address) throws IOException {
        
        if (userExists(username)) {
            throw new IllegalArgumentException("User already exists: " + username);
        }
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_ROUNDS));
        String record = String.format("%s,%s,%s,%s,%s%n", 
            username, hashedPassword, contact, email, address);
        
        Files.write(USERS_FILE, record.getBytes(), StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        logger.info("Created user: {}", username);
    }
    
    public static boolean authenticate(String username, String password) throws IOException {
        Optional<UserRecord> user = findUser(username);
        if (user.isEmpty()) {
            return false;
        }
        
        boolean valid = BCrypt.checkpw(password, user.get().hashedPassword);
        if (valid) {
            logger.info("User authenticated: {}", username);
        } else {
            logger.warn("Authentication failed for user: {}", username);
        }
        return valid;
    }
    
    public static boolean userExists(String username) throws IOException {
        return findUser(username).isPresent();
    }
    
    public static Optional<UserRecord> findUser(String username) throws IOException {
        if (!Files.exists(USERS_FILE)) {
            return Optional.empty();
        }
        
        List<String> lines = Files.readAllLines(USERS_FILE);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            String[] parts = line.split(",", 5);
            if (parts.length >= 2 && parts[0].equals(username)) {
                return Optional.of(new UserRecord(
                    parts[0],
                    parts[1],
                    parts.length > 2 ? parts[2] : "",
                    parts.length > 3 ? parts[3] : "",
                    parts.length > 4 ? parts[4] : ""
                ));
            }
        }
        return Optional.empty();
    }
    
    public static final class UserRecord {
        public final String username;
        public final String hashedPassword;
        public final String contact;
        public final String email;
        public final String address;
        
        public UserRecord(String username, String hashedPassword, String contact, 
                         String email, String address) {
            this.username = username;
            this.hashedPassword = hashedPassword;
            this.contact = contact;
            this.email = email;
            this.address = address;
        }
    }
}

