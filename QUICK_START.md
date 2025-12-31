# Quick Start Guide

## Prerequisites

- **Java 17+** (LTS recommended)
- **Maven 3.6+** (for building)

Verify installation:
```bash
java -version  # Should show 17 or higher
mvn -version    # Should show 3.6 or higher
```

## Building the Project

```bash
# Navigate to project directory
cd Video-Encryption-using-Quantum-Cryptography

# Clean and compile
mvn clean compile

# Run tests
mvn test

# Create executable JAR
mvn clean package
```

The JAR file will be created at: `target/video-encryption-2.0.0.jar`

## Running the Application

```bash
# Using Maven
mvn exec:java -Dexec.mainClass="com.pqc.videoencryption.Main"

# Or using the JAR
java -jar target/video-encryption-2.0.0.jar
```

## First-Time Setup

1. **Register a new user**:
   - Click "Register"
   - Fill in username, password, contact, email, and address
   - Click "Register"

2. **Login**:
   - Enter your username and password
   - Click "Login"

3. **Encrypt a video**:
   - Click "Select Video File"
   - Choose a video file
   - Click "Encrypt Video (PQC)"
   - Encrypted file saved to `encryptedVideos/`

4. **Decrypt a video**:
   - Click "Decrypt Video (PQC)"
   - Select an encrypted file
   - Decrypted file saved to `decryptedVideos/`

## Directory Structure

The application creates these directories automatically:
- `keys/` - User keystores (.p12 files)
- `data/` - User database
- `encryptedVideos/` - Encrypted video files
- `decryptedVideos/` - Decrypted video files

## Troubleshooting

### "Java version not supported"
- Ensure Java 17+ is installed
- Check `JAVA_HOME` environment variable

### "Maven not found"
- Install Maven or use Maven wrapper
- Verify Maven is in PATH

### "BouncyCastle provider not found"
- Run `mvn clean install` to download dependencies
- Check internet connection for Maven downloads

### "Keystore not found"
- Register a new user first
- Keystore is created automatically on first encryption

## Testing

Run unit tests:
```bash
mvn test
```

Run specific test class:
```bash
mvn test -Dtest=CryptoTest
```

## Development

### IDE Setup
1. Import as Maven project
2. Ensure Java 17 SDK is configured
3. Maven dependencies will download automatically

### Code Style
- Follow existing code style
- Use meaningful variable names
- Add Javadoc for public methods

## Security Notes

- **Never commit** `keys/`, `data/`, or encrypted files to version control
- **Backup keystores** - losing the keystore means losing access to encrypted files
- **Strong passwords** - use strong passwords for user accounts

## Next Steps

- Read `README.md` for detailed documentation
- Review `ARCHITECTURE.md` for system design
- Check `IMPLEMENTATION_GUIDE.md` for implementation details
- See `REFACTORING_SUMMARY.md` for what changed

## Support

For issues or questions:
1. Check documentation files
2. Review error messages in logs
3. Verify Java and Maven versions

---

**Version**: 2.0.0  
**Last Updated**: 2024

