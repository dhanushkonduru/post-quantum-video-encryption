# Implementation Guide

## Cryptographic Implementation Details

### Kyber-1024 Key Exchange

```java
// Key pair generation
KeyPairGenerator kpg = KeyPairGenerator.getInstance("KYBER1024", PQC_PROVIDER);
kpg.initialize(KyberParameterSpec.kyber1024, new SecureRandom());
KeyPair keyPair = kpg.generateKeyPair();

// Encapsulation (sender)
Cipher kem = Cipher.getInstance("KYBER1024", PQC_PROVIDER);
kem.init(Cipher.ENCRYPT_MODE, recipientPublicKey, new SecureRandom());
byte[] sharedSecret = kem.doFinal();

// Decapsulation (receiver)
Cipher kem = Cipher.getInstance("KYBER1024", PQC_PROVIDER);
kem.init(Cipher.DECRYPT_MODE, privateKey);
byte[] sharedSecret = kem.doFinal(encapsulatedKey);
```

### AES-256-GCM Encryption

```java
// Key creation
SecretKey key = new SecretKeySpec(keyMaterial, "AES");

// Encryption
byte[] iv = new byte[12];
SecureRandom.getInstanceStrong().nextBytes(iv);
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", BC_PROVIDER);
GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
byte[] ciphertext = cipher.doFinal(plaintext);

// Decryption
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", BC_PROVIDER);
GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
byte[] plaintext = cipher.doFinal(ciphertext);
```

### HKDF Key Derivation

```java
// Extract phase
Mac hmac = Mac.getInstance("HmacSHA256");
hmac.init(new SecretKeySpec(salt, "HmacSHA256"));
byte[] prk = hmac.doFinal(sharedSecret);

// Expand phase
byte[] okm = new byte[keyLength];
byte[] t = new byte[0];
for (int i = 0; i < n; i++) {
    hmac.init(new SecretKeySpec(prk, "HmacSHA256"));
    hmac.update(t);
    if (info != null) hmac.update(info);
    hmac.update((byte)(i + 1));
    t = hmac.doFinal();
    System.arraycopy(t, 0, okm, i * 32, Math.min(32, keyLength - i * 32));
}
```

## Security-Critical Implementation Details

### IV Generation

**Critical**: IVs must be:
- **Unique**: Never reuse an IV with the same key
- **Unpredictable**: Use cryptographically secure random number generator
- **Correct Size**: 96 bits (12 bytes) for GCM

```java
SecureRandom secureRandom = new SecureRandom();
byte[] iv = new byte[12];
secureRandom.nextBytes(iv);
```

### Key Wiping

**Critical**: Always wipe sensitive data from memory after use.

```java
try {
    // Use key
} finally {
    if (keyMaterial != null) {
        Arrays.fill(keyMaterial, (byte) 0);
    }
}
```

### Error Handling

**Critical**: Never leak sensitive information in error messages.

```java
// BAD
throw new Exception("Decryption failed with key: " + key.toString());

// GOOD
logger.error("Decryption failed", e);
throw new GeneralSecurityException("Decryption failed");
```

## Cross-Platform File Operations

### Use java.nio.file.Path

```java
// BAD (Windows-specific)
File file = new File("keys\\user.p12");

// GOOD (Cross-platform)
Path path = Paths.get("keys", "user.p12");
```

### Directory Creation

```java
Path dir = Paths.get("encryptedVideos");
if (!Files.exists(dir)) {
    Files.createDirectories(dir);
}
```

### File Reading/Writing

```java
// Reading
byte[] data = Files.readAllBytes(inputPath);

// Writing
Files.write(outputPath, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
```

## Testing Strategy

### Unit Tests

Test each cryptographic primitive independently:

```java
@Test
public void testEncryptionDecryption() {
    // Test encryption produces valid ciphertext
    // Test decryption recovers original plaintext
    // Test IV uniqueness
    // Test authentication tag verification
}
```

### Integration Tests

Test end-to-end workflows:

```java
@Test
public void testVideoEncryptionPipeline() {
    // Create test video file
    // Encrypt video
    // Decrypt video
    // Verify integrity
}
```

### Security Tests

Verify security properties:

```java
@Test
public void testKeyWiping() {
    // Verify keys are zeroed after use
}

@Test
public void testIVUniqueness() {
    // Verify IVs are unique across encryptions
}
```

## Performance Benchmarks

### Expected Performance

- **Kyber-1024 Key Generation**: 50-100ms
- **AES-256-GCM Encryption**: ~100-500 MB/s (hardware-dependent)
- **HKDF Key Derivation**: <1ms

### Benchmarking Code

```java
long start = System.nanoTime();
// Cryptographic operation
long duration = System.nanoTime() - start;
double throughput = (dataSize / (1024.0 * 1024.0)) / (duration / 1e9);
System.out.println("Throughput: " + throughput + " MB/s");
```

## Common Pitfalls

### 1. IV Reuse

**Never reuse IVs with the same key**. This breaks GCM security.

### 2. Key Material Leakage

**Always wipe keys** from memory after use.

### 3. Error Information Leakage

**Never include sensitive data** in error messages or logs.

### 4. Platform-Specific Paths

**Always use Path/Paths** instead of File for cross-platform compatibility.

### 5. Synchronous I/O

**Consider background threads** for large file operations to avoid UI blocking.

## Viva Defense Preparation

### Key Talking Points

1. **Why Post-Quantum?**
   - Quantum computers threaten classical cryptography
   - NIST standardization ensures algorithm security
   - Forward security against future threats

2. **Algorithm Selection**
   - Kyber-1024: NIST Level 5, standardized, efficient
   - AES-256-GCM: Industry standard, quantum-resistant key size
   - HKDF: Standard key derivation (RFC 5869)

3. **Security Model**
   - Computational security (not information-theoretic)
   - Protection against quantum adversaries
   - Authenticated encryption (confidentiality + integrity)

4. **Implementation Quality**
   - Secure defaults
   - Key wiping
   - Proper error handling
   - Cross-platform compatibility

### Common Questions and Answers

**Q: Why not use quantum key distribution (QKD)?**
A: QKD requires specialized hardware and infrastructure. PQC provides software-based security using standardized algorithms.

**Q: Is this truly "quantum" cryptography?**
A: No. This is **post-quantum** cryptography - classical algorithms designed to resist quantum attacks. No quantum hardware or mechanics are involved.

**Q: What security level does this provide?**
A: NIST Level 5, equivalent to AES-256. This provides strong security against both classical and quantum adversaries.

**Q: Why AES-256 instead of a post-quantum symmetric cipher?**
A: AES-256 with a 256-bit key is already considered quantum-resistant. The quantum threat primarily affects public-key cryptography.

**Q: How do you handle key management?**
A: Keys are stored in PKCS12 keystores protected by user passwords. Passwords are hashed with bcrypt. Keys are wiped from memory after use.

