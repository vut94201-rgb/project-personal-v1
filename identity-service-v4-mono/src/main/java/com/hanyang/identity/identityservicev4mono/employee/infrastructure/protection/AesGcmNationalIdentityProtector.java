package com.hanyang.identity.identityservicev4mono.employee.infrastructure.protection;


import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class AesGcmNationalIdentityProtector implements NationalIdentityProtectionPort {

    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;
    private static final String PAYLOAD_VERSION = "v1";

    private final NationalIdentityProtectionProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encrypt(String normalizedNumber) {
        if (normalizedNumber == null || normalizedNumber.isBlank()) {
            throw new IllegalArgumentException("normalizedNumber must not be blank");
        }

        try {
            byte[] encryptionKey = decodeEncryptionKey();
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKey secretKey = new SecretKeySpec(encryptionKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(normalizedNumber.getBytes(StandardCharsets.UTF_8));

            String encryptedNumber = String.join(":",
                    PAYLOAD_VERSION,
                    Base64.getEncoder().encodeToString(iv),
                    Base64.getEncoder().encodeToString(ciphertext)
            );

            return encryptedNumber;
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to protect national identity number", ex);
        }
    }

    String decryptForVerification(String encryptedNumber) {
        if (encryptedNumber == null || encryptedNumber.isBlank()) {
            throw new IllegalArgumentException("encryptedNumber must not be blank");
        }

        String[] parts = encryptedNumber.split(":", -1);
        if (parts.length != 3 || !PAYLOAD_VERSION.equals(parts[0])) {
            throw new IllegalArgumentException("Unsupported national identity ciphertext format");
        }

        try {
            byte[] iv = Base64.getDecoder().decode(parts[1]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[2]);

            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            SecretKey secretKey = new SecretKeySpec(decodeEncryptionKey(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to reveal national identity number", ex);
        }
    }

    @Override
    public String fingerprint(String normalizedNumber) {
        if (normalizedNumber == null || normalizedNumber.isBlank()) {
            throw new IllegalArgumentException("normalizedNumber must not be blank");
        }

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(decodeFingerprintKey(), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(
                    mac.doFinal(normalizedNumber.getBytes(StandardCharsets.UTF_8))
            );
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to fingerprint national identity number", ex);
        }
    }

    private byte[] decodeEncryptionKey() {
        byte[] key = decodeConfiguredKey(properties.encryptionKey(), "encryption key");
        if (key.length != 32) {
            throw new IllegalStateException("National identity encryption key must decode to exactly 32 bytes");
        }
        return key;
    }

    private byte[] decodeFingerprintKey() {
        byte[] key = decodeConfiguredKey(properties.fingerprintKey(), "fingerprint key");
        if (key.length < 32) {
            throw new IllegalStateException("National identity fingerprint key must decode to at least 32 bytes");
        }
        return key;
    }

    private static byte[] decodeConfiguredKey(String configuredValue, String label) {
        if (configuredValue == null || configuredValue.isBlank()) {
            throw new IllegalStateException("National identity " + label + " is not configured");
        }

        try {
            return Base64.getDecoder().decode(configuredValue.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("National identity " + label + " must be Base64 encoded", ex);
        }
    }
}