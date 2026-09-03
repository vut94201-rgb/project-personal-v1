package com.hanyang.identity.identityservicev4mono.employee.application.port;
public interface NationalIdentityProtectionPort {

    String encrypt(String normalizedNumber);

    String fingerprint(String normalizedNumber);
}