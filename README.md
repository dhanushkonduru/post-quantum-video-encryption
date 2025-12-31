# Post-Quantum Video Encryption System

## Overview

A production-grade video encryption system implementing **NIST-standardized Post-Quantum Cryptography (PQC)** algorithms. This system provides computational security against both classical and quantum adversaries using a hybrid cryptographic architecture.

## Important Disclaimers

**This system does NOT:**
- Use quantum hardware or quantum computing
- Implement quantum key distribution (QKD)
- Provide information-theoretic security
- Use quantum mechanics in any form

**This system DOES:**
- Use **post-quantum cryptographic algorithms** (classical algorithms resistant to quantum attacks)
- Implement **Kyber-1024** (NIST PQC Standard, Level 5 security)
- Use **AES-256-GCM** for authenticated symmetric encryption
- Provide **computational security** against quantum adversaries
- Follow **NIST PQC standards** and best practices

## Technical Architecture

### Cryptographic Components

1. **Key Encapsulation Mechanism (KEM)**
   - Algorithm: **Kyber-1024** (NIST PQC Standard)
   - Security Level: NIST Level 5 (equivalent to AES-256)
   - Provider: BouncyCastle PQC Provider 1.78+

2. **Symmetric Encryption**
   - Algorithm: **AES-256-GCM**
   - Key Size: 256 bits
   - IV Size: 96 bits (12 bytes)
   - Authentication Tag: 128 bits
   - Provides both confidentiality and authenticity

3. **Key Derivation**
   - Algorithm: **HKDF-SHA256** (RFC 5869)
   - Expands shared secrets into symmetric keys
   - Uses salt and context information

### System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    User Interface Layer                  │
│  (LoginFrame, RegisterFrame, UserScreenFrame)          │
└────────────────────┬──────────────────────────────────┘
                     │
┌────────────────────▼──────────────────────────────────┐
│              Video Encryption Service                  │
│  (Orchestrates encryption/decryption operations)      │
└────────────────────┬──────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼────────┐      ┌────────▼──────────┐
│  PQC Key       │      │  Symmetric        │
│  Exchange      │      │  Encryption       │
│  (Kyber-1024)  │      │  (AES-256-GCM)    │
└────────────────┘      └───────────────────┘
        │                         │
        └────────────┬────────────┘
                     │
        ┌────────────▼────────────┐
        │   Key Derivation        │
        │   (HKDF-SHA256)         │
        └─────────────────────────┘
                     │
        ┌────────────▼────────────┐
        │   Key Storage            │
        │   (PKCS12 Keystore)      │
        └─────────────────────────┘
```

## Requirements

- **Java**: 17 LTS or higher
- **Maven**: 3.6+ (for building)
- **Operating System**: macOS, Linux, or Windows

## Dependencies

- **BouncyCastle**: 1.78+ (bcprov-jdk18on, bcpkix-jdk18on)
- **SLF4J**: 2.0.9+ (logging)
- **Logback**: 1.4.11+ (logging implementation)
- **jBCrypt**: 0.4 (password hashing)

See `pom.xml` for complete dependency list.

## Building the Project

```bash
# Clone or navigate to the project directory
cd Video-Encryption-using-Quantum-Cryptography

# Build with Maven
mvn clean compile

# Create executable JAR
mvn clean package

# Run the application
java -jar target/video-encryption-2.0.0.jar
```

## Usage

1. **Register a new user**: Click "Register" and provide username, password, and contact information
2. **Login**: Enter your credentials
3. **Select video**: Click "Select Video File" to choose a video file
4. **Encrypt**: Click "Encrypt Video (PQC)" to encrypt the selected video
5. **Decrypt**: Click "Decrypt Video (PQC)" and select an encrypted file

Encrypted videos are stored in `encryptedVideos/` directory.
Decrypted videos are stored in `decryptedVideos/` directory.

## Security Features

### Key Management
- Keys stored in PKCS12 keystores protected by user passwords
- AES keys generated using `SecureRandom`
- Keys wiped from memory after use
- Password hashing using bcrypt (cost factor 12)

### Encryption Security
- **Authenticated Encryption**: AES-GCM provides both confidentiality and authenticity
- **Unique IVs**: Each encryption uses a cryptographically secure random IV
- **Key Derivation**: HKDF ensures proper key expansion
- **Post-Quantum Security**: Kyber-1024 provides security against quantum attacks

### Implementation Security
- No information leakage in error messages
- Secure memory handling (key wiping)
- Cross-platform file operations (java.nio.file)
- Input validation and sanitization

## Limitations and Scope

### What This System Provides
- ✅ Video file encryption/decryption
- ✅ Post-quantum key exchange capability (Kyber-1024)
- ✅ Authenticated encryption (AES-256-GCM)
- ✅ Secure key storage
- ✅ Password-based authentication

### What This System Does NOT Provide
- ❌ Real-time video streaming encryption
- ❌ Multi-user key sharing (requires additional implementation)
- ❌ Quantum hardware integration
- ❌ Information-theoretic security
- ❌ Quantum key distribution (QKD)

## Academic Defense Points

### Why Post-Quantum Cryptography?
1. **Quantum Threat**: Large-scale quantum computers (when available) can break RSA, ECC, and other classical public-key algorithms
2. **NIST Standardization**: Kyber-1024 is a NIST-selected PQC algorithm (2022)
3. **Hybrid Approach**: Combines PQC (key exchange) with classical symmetric encryption (AES)
4. **Forward Security**: Protects against future quantum attacks

### Algorithm Selection Rationale
- **Kyber-1024**: Selected for NIST Level 5 security, efficient performance, and standardization
- **AES-256-GCM**: Industry standard for authenticated encryption, quantum-resistant key size
- **HKDF-SHA256**: Standard key derivation function (RFC 5869)

### Security Model
- **Threat Model**: Protection against both classical and quantum adversaries
- **Security Level**: NIST Level 5 (equivalent to AES-256)
- **Assumptions**: Computational security (not information-theoretic)

## Testing

### Unit Tests
```bash
mvn test
```

### Manual Testing Checklist
- [ ] User registration and login
- [ ] Video encryption (various file sizes)
- [ ] Video decryption (verify integrity)
- [ ] Cross-platform compatibility (macOS, Linux, Windows)
- [ ] Error handling (invalid files, wrong passwords)

## Performance Considerations

- **Key Generation**: Kyber-1024 key pair generation: ~50-100ms
- **Encryption Throughput**: Depends on file size and hardware
- **Memory Usage**: Entire video loaded into memory (consider streaming for large files)

## File Structure

```
src/main/java/com/pqc/videoencryption/
├── crypto/
│   ├── CryptoConstants.java          # Algorithm identifiers
│   ├── PostQuantumKeyExchange.java   # Kyber-1024 KEM
│   ├── SymmetricEncryption.java      # AES-256-GCM
│   ├── KeyDerivation.java            # HKDF-SHA256
│   └── VideoEncryptionService.java   # High-level encryption service
├── storage/
│   ├── KeyStoreManager.java          # PKCS12 keystore operations
│   └── UserRepository.java            # User management
├── ui/
│   ├── LoginFrame.java               # Login interface
│   ├── RegisterFrame.java            # Registration interface
│   └── UserScreenFrame.java          # Main user interface
└── Main.java                         # Application entry point
```

## Contributing

This is an academic/research project. For production use, consider:
- Adding streaming support for large files
- Implementing multi-user key sharing
- Adding comprehensive test coverage
- Security audit by qualified cryptographers

## License

[Specify your license here]

## References

1. NIST Post-Quantum Cryptography Standardization: https://csrc.nist.gov/projects/post-quantum-cryptography
2. Kyber Algorithm Specification: https://pq-crystals.org/kyber/
3. RFC 5869 - HKDF: https://tools.ietf.org/html/rfc5869
4. NIST SP 800-38D (GCM): https://csrc.nist.gov/publications/detail/sp/800-38d/final

## Contact

[Your contact information]

---

**Version**: 2.0.0  
**Last Updated**: 2024  
**Java Version**: 17 LTS  
**Status**: Production-ready architecture
