# Cryptographic Architecture Audit Report

## Executive Summary

The existing codebase incorrectly labels itself as "Quantum Video Encryption" while implementing classical cryptography (Threefish-1024). This audit identifies all issues and provides a roadmap for refactoring into a production-grade Post-Quantum Cryptography (PQC) system.

## Critical Issues Identified

### 1. Misleading Terminology and Claims
- **Class Name**: `QuantumEncryption` - No quantum operations exist
- **UI Text**: "Video Encryption using Quantum Computing" - False claim
- **Comments**: References to "quantum computers" in code (line 56 of QuantumEncryption.java)
- **Project Name**: "Video-Encryption-using-Quantum-Cryptography" - Misleading

**Impact**: Academic dishonesty, false security claims, potential viva failure

### 2. Cryptographic Implementation Issues

#### Current Implementation (Threefish-1024)
- **Algorithm**: Threefish-1024/CBC/PKCS7Padding
- **Key Size**: 1024 bits
- **IV Size**: 128 bytes (excessive, should be 16 bytes for most block ciphers)
- **Provider**: BouncyCastle 1.61 (outdated, security vulnerabilities)

#### Problems:
1. Threefish is a classical cipher, not quantum-resistant
2. CBC mode vulnerable to padding oracle attacks
3. No authentication (MAC) - vulnerable to tampering
4. IV size of 128 bytes is incorrect for Threefish (should match block size)
5. No key exchange mechanism - keys stored in plaintext keystores
6. No forward secrecy
7. Keys derived only from username/password (weak entropy)

### 3. Platform-Specific Code

#### Windows Dependencies:
- `run.bat` with hardcoded JDK path: `C:\Program Files\Java\jdk-14.0.2\bin`
- Hardcoded file paths: `"keys/"`, `"EncryptedVideos/"`, `"testVideos/"`
- File separators not using `File.separator` or `Path`

**Impact**: Non-functional on macOS/Linux

### 4. Java Version Issues
- **Current**: Java 14 (hardcoded in run.bat)
- **Required**: Java 17+ LTS for production PQC support
- **BouncyCastle**: Version 1.61 (2018) - missing PQC algorithms

### 5. Architecture Problems

#### Package Structure:
- Single package `com` - no separation of concerns
- UI, cryptography, and file I/O mixed together
- No interfaces or abstractions
- No dependency injection

#### Security Issues:
- Passwords stored in plaintext (`Users.txt`)
- No password hashing (bcrypt, Argon2)
- Keystore password = user password (weak security model)
- No key rotation mechanism
- No secure key deletion (keys remain in memory)

### 6. File I/O Issues
- Uses deprecated `java.io.File` instead of `java.nio.file.Path`
- No exception handling for file operations
- Synchronous I/O (blocks UI thread for large videos)
- No progress tracking for large files

### 7. Missing Features
- No key exchange protocol
- No digital signatures for integrity
- No metadata encryption
- No streaming support (loads entire video into memory)
- No error recovery

## Required Changes

### Files to Remove/Rename:
1. `QuantumEncryption.java` → `PostQuantumEncryption.java`
2. `run.bat` → Remove (replace with Maven/Gradle)
3. All `.class` files → Remove (regenerate with new build)

### Files to Modify:
1. `Identifiers.java` - Replace all Threefish constants with PQC constants
2. `Login.java` - Remove "Quantum" terminology
3. `UserScreen.java` - Update button text and file operations
4. `Register.java` - Add password hashing
5. All files - Replace `java.io.File` with `java.nio.file.Path`

### New Architecture Required:

```
src/main/java/
├── com.pqc.videoencryption/
│   ├── crypto/
│   │   ├── PostQuantumKeyExchange.java      (Kyber-1024 KEM)
│   │   ├── SymmetricEncryption.java         (AES-256-GCM)
│   │   ├── KeyDerivation.java               (HKDF-SHA256)
│   │   └── CryptoConstants.java            (Algorithm identifiers)
│   ├── storage/
│   │   ├── KeyStoreManager.java            (Secure key storage)
│   │   └── UserRepository.java             (User management)
│   ├── ui/
│   │   ├── LoginFrame.java
│   │   ├── RegisterFrame.java
│   │   └── UserScreenFrame.java
│   └── Main.java
```

## Dependencies Required

### Maven Dependencies:
```xml
<dependencies>
    <!-- BouncyCastle PQC Provider -->
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcprov-jdk18on</artifactId>
        <version>1.78</version>
    </dependency>
    <dependency>
        <groupId>org.bouncycastle</groupId>
        <artifactId>bcpkix-jdk18on</artifactId>
        <version>1.78</version>
    </dependency>
    
    <!-- Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>2.0.9</version>
    </dependency>
    <dependency>
        <groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.4.11</version>
    </dependency>
    
    <!-- Password Hashing -->
    <dependency>
        <groupId>org.mindrot</groupId>
        <artifactId>jbcrypt</artifactId>
        <version>0.4</version>
    </dependency>
</dependencies>
```

### Build Configuration:
- **Java Version**: 17 (LTS)
- **Maven Compiler**: 3.11.0
- **Encoding**: UTF-8

## Security Improvements Required

1. **Key Exchange**: Implement Kyber-1024 KEM for secure key establishment
2. **Symmetric Encryption**: AES-256-GCM (authenticated encryption)
3. **Key Derivation**: HKDF-SHA256 for key expansion
4. **Password Storage**: bcrypt with cost factor 12+
5. **IV Generation**: SecureRandom, 96-bit IVs for GCM
6. **Key Wiping**: Zero out sensitive data after use
7. **Error Handling**: No information leakage in exceptions

## Testing Requirements

1. **Unit Tests**: All cryptographic operations
2. **Integration Tests**: End-to-end encryption/decryption
3. **Security Tests**: Verify key wiping, IV uniqueness
4. **Performance Tests**: Throughput benchmarks
5. **Cross-Platform Tests**: macOS, Linux, Windows

## Documentation Updates

1. **README.md**: Accurate technical description
2. **ARCHITECTURE.md**: System design documentation
3. **SECURITY.md**: Threat model and mitigations
4. **API.md**: Public interface documentation

## Migration Path

1. Phase 1: Set up Maven build and dependencies
2. Phase 2: Implement PQC cryptographic modules
3. Phase 3: Refactor UI components
4. Phase 4: Replace file I/O operations
5. Phase 5: Add testing framework
6. Phase 6: Update documentation
7. Phase 7: Cross-platform validation

## Risk Assessment

**High Risk**:
- Current implementation provides false security guarantees
- Academic integrity violation if presented as "quantum"
- No protection against quantum adversaries

**Medium Risk**:
- Platform lock-in prevents deployment
- Outdated dependencies have known vulnerabilities

**Low Risk**:
- UI improvements (cosmetic)

## Conclusion

Complete refactoring required. The current codebase cannot be incrementally fixed - it requires architectural redesign with proper PQC algorithms, secure key management, and cross-platform compatibility.

