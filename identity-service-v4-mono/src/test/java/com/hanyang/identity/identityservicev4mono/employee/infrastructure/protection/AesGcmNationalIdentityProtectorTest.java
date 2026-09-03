package com.hanyang.identity.identityservicev4mono.employee.infrastructure.protection;


import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AesGcmNationalIdentityProtectorTest {

    @Test
    void encryptsWithRandomIvButProducesDeterministicFingerprint() {
        AesGcmNationalIdentityProtector protector = new AesGcmNationalIdentityProtector(
                validProperties()
        );

        String firstEncrypted = protector.encrypt("001204012345");
        String secondEncrypted = protector.encrypt("001204012345");
        String firstFingerprint = protector.fingerprint("001204012345");
        String secondFingerprint = protector.fingerprint("001204012345");

        assertNotEquals(firstEncrypted, secondEncrypted);
        assertEquals(firstFingerprint, secondFingerprint);
        assertEquals("001204012345", protector.decryptForVerification(firstEncrypted));
        assertEquals("001204012345", protector.decryptForVerification(secondEncrypted));
    }

    @Test
    void failsClosedWhenKeysAreNotConfigured() {
        AesGcmNationalIdentityProtector protector = new AesGcmNationalIdentityProtector(
                new NationalIdentityProtectionProperties("", "")
        );

        assertThrows(IllegalStateException.class, () -> protector.encrypt("001204012345"));
        assertThrows(IllegalStateException.class, () -> protector.fingerprint("001204012345"));
    }

    private static NationalIdentityProtectionProperties validProperties() {
        String encryptionKey = Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)
        );
        String fingerprintKey = Base64.getEncoder().encodeToString(
                "abcdef0123456789abcdef0123456789".getBytes(StandardCharsets.UTF_8)
        );
        return new NationalIdentityProtectionProperties(encryptionKey, fingerprintKey);
    }
}