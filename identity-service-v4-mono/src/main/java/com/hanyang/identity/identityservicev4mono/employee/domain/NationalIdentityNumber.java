package com.hanyang.identity.identityservicev4mono.employee.domain;


import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Transient value object used to normalize and validate a national identity number
 * before it is protected for persistence. The raw value must never be persisted or logged.
 */
public record NationalIdentityNumber(String value, String lastFour) {

    private static final Pattern VIETNAM_CITIZEN_ID = Pattern.compile("^[0-9]{12}$");

    public NationalIdentityNumber {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(lastFour, "lastFour must not be null");
    }

    public static NationalIdentityNumber of(
            String countryCode,
            NationalIdentityType identityType,
            String rawValue
    ) {
        String normalizedCountryCode = normalizeCountryCode(countryCode);
        Objects.requireNonNull(identityType, "identityType must not be null");

        if (rawValue == null) {
            throw new IllegalArgumentException("national identity number must not be null");
        }

        String normalizedValue = rawValue.trim();
        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("national identity number must not be blank");
        }

        if ("VN".equals(normalizedCountryCode)
                && identityType == NationalIdentityType.NATIONAL_ID_CARD
                && !VIETNAM_CITIZEN_ID.matcher(normalizedValue).matches()) {
            throw new IllegalArgumentException("Vietnam national identity card number must contain exactly 12 digits");
        }

        if (!"VN".equals(normalizedCountryCode)) {
            throw new IllegalArgumentException("unsupported national identity country: " + normalizedCountryCode);
        }

        return new NationalIdentityNumber(
                normalizedValue,
                normalizedValue.substring(normalizedValue.length() - 4)
        );
    }

    public static String normalizeCountryCode(String countryCode) {
        if (countryCode == null) {
            throw new IllegalArgumentException("countryCode must not be null");
        }

        String normalized = countryCode.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("countryCode must be an ISO alpha-2 code");
        }
        return normalized;
    }
}