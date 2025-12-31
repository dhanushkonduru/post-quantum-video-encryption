# System Architecture Documentation

## Design Principles

1. **Separation of Concerns**: Clear separation between UI, business logic, and cryptographic operations
2. **Modularity**: Each cryptographic component is independently testable
3. **Security First**: Secure defaults, key wiping, proper error handling
4. **Cross-Platform**: Uses java.nio.file for all file operations
5. **Standards Compliance**: Follows NIST PQC standards and RFC specifications

## Package Structure

### `com.pqc.videoencryption.crypto`

Cryptographic primitives and operations.

#### `CryptoConstants`
- Defines algorithm identifiers
- Provider configuration
- Key sizes and parameters

#### `PostQuantumKeyExchange`
- Kyber-1024 key pair generation
- Key encapsulation/decapsulation
- Public key encoding/decoding

#### `SymmetricEncryption`
- AES-256-GCM encryption/decryption
- IV generation (96-bit, cryptographically secure)
- Key creation from material

#### `KeyDerivation`
- HKDF-SHA256 implementation
- Key expansion from shared secrets
- Salt and context handling

#### `VideoEncryptionService`
- High-level encryption/decryption API
- File format handling
- Integration with key storage

### `com.pqc.videoencryption.storage`

Persistent storage and key management.

#### `KeyStoreManager`
- PKCS12 keystore operations
- AES key generation and storage
- Kyber key pair management

#### `UserRepository`
- User account management
- Password hashing (bcrypt)
- User authentication

### `com.pqc.videoencryption.ui`

User interface components (Swing).

#### `LoginFrame`
- User authentication UI
- Navigation to registration

#### `RegisterFrame`
- New user registration
- Input validation

#### `UserScreenFrame`
- Video file selection
- Encryption/decryption operations
- Activity logging

## Data Flow

### Encryption Flow

```
User selects video file
    ↓
VideoEncryptionService.encryptVideo()
    ↓
Load/Create keystore → KeyStoreManager.loadAESKey()
    ↓
Read video file → byte[]
    ↓
SymmetricEncryption.encrypt(key, videoData)
    ├─ Generate 96-bit IV (SecureRandom)
    ├─ Initialize AES-256-GCM cipher
    └─ Encrypt with authentication tag
    ↓
Serialize (ciphertext + IV + filename)
    ↓
Write to encryptedVideos/
    ↓
Wipe sensitive data from memory
```

### Decryption Flow

```
User selects encrypted file
    ↓
VideoEncryptionService.decryptVideo()
    ↓
Load keystore → KeyStoreManager.loadAESKey()
    ↓
Read encrypted file → byte[]
    ↓
Deserialize (ciphertext, IV, filename)
    ↓
SymmetricEncryption.decrypt(key, ciphertext, IV)
    ├─ Initialize AES-256-GCM cipher
    ├─ Verify authentication tag
    └─ Decrypt to plaintext
    ↓
Write to decryptedVideos/
    ↓
Wipe sensitive data from memory
```

## Security Architecture

### Key Hierarchy

```
User Password (bcrypt hashed)
    ↓
PKCS12 Keystore (password-protected)
    ├─ AES-256 Key (for video encryption)
    └─ Kyber-1024 Key Pair (for key exchange)
```

### Memory Security

- Keys wiped after use (`Arrays.fill()`)
- Sensitive data cleared in `finally` blocks
- No key material in logs or error messages

### Error Handling

- No information leakage in exceptions
- Generic error messages for users
- Detailed logging for debugging (without sensitive data)

## File Formats

### Encrypted Video Format

```
[4 bytes: filename length]
[filename bytes]
[4 bytes: IV length]
[12 bytes: IV]
[4 bytes: ciphertext length]
[ciphertext bytes (includes GCM tag)]
```

### Keystore Format

- **Type**: PKCS12 (.p12)
- **Aliases**:
  - `AES-Key`: Symmetric encryption key
  - `Kyber-KeyPair`: Post-quantum key pair (optional)

### User Database Format

- **File**: `data/users.txt`
- **Format**: CSV (username, bcrypt_hash, contact, email, address)
- **Security**: Passwords hashed with bcrypt (cost factor 12)

## Extension Points

### Adding Key Sharing

1. Implement key exchange protocol using `PostQuantumKeyExchange`
2. Add user-to-user key sharing in `VideoEncryptionService`
3. Extend `KeyStoreManager` to handle shared keys

### Adding Streaming Support

1. Modify `VideoEncryptionService` to process chunks
2. Use `Cipher.update()` for incremental encryption
3. Add progress callbacks for UI updates

### Adding Digital Signatures

1. Implement SPHINCS+ or Dilithium (NIST PQC signatures)
2. Add signature generation/verification in crypto package
3. Integrate with video encryption service

## Performance Considerations

### Current Limitations

- **Memory**: Entire video loaded into memory
- **Synchronous I/O**: Blocks UI thread during encryption
- **No Progress Tracking**: Large files show no progress

### Optimization Opportunities

- Streaming encryption for large files
- Background threads for encryption operations
- Progress bars with chunk-based processing
- Memory-mapped file I/O for very large files

## Testing Strategy

### Unit Tests

- Cryptographic primitives (encryption, decryption, key derivation)
- Key store operations
- User repository operations

### Integration Tests

- End-to-end encryption/decryption
- File format serialization/deserialization
- Cross-platform file operations

### Security Tests

- Key wiping verification
- IV uniqueness
- Authentication tag verification
- Password hashing validation

## Deployment

### Build Artifacts

- `target/video-encryption-2.0.0.jar`: Executable JAR
- `target/classes/`: Compiled classes
- `target/test-classes/`: Test classes

### Runtime Requirements

- Java 17+ JRE
- Sufficient memory for video files
- Disk space for encrypted/decrypted videos

### Directory Structure (Runtime)

```
project-root/
├── keys/              # User keystores (.p12 files)
├── data/              # User database
├── testVideos/        # Input videos (user-created)
├── encryptedVideos/   # Encrypted output
└── decryptedVideos/   # Decrypted output
```

## Future Enhancements

1. **Multi-user Key Sharing**: Implement Kyber-based key exchange between users
2. **Streaming Support**: Process large videos in chunks
3. **Metadata Encryption**: Encrypt video metadata separately
4. **Key Rotation**: Implement key rotation policies
5. **Audit Logging**: Comprehensive security audit trail
6. **Performance Monitoring**: Metrics and benchmarking tools

