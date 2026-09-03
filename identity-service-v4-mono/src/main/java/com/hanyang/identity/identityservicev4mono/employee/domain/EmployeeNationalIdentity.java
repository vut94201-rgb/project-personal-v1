package com.hanyang.identity.identityservicev4mono.employee.domain;



import java.util.Objects;

public class EmployeeNationalIdentity {

    private final EmployeeNationalIdentityId id;
    private final EmployeeId employeeId;
    private final String countryCode;
    private final NationalIdentityType identityType;

    private String encryptedNumber;
    private String numberFingerprint;
    private String lastFour;

    private EmployeeNationalIdentity(
            EmployeeNationalIdentityId id,
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType,
            String encryptedNumber,
            String numberFingerprint,
            String lastFour
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.countryCode = NationalIdentityNumber.normalizeCountryCode(countryCode);
        this.identityType = Objects.requireNonNull(identityType, "identityType must not be null");
        updateProtectedNumber(encryptedNumber, numberFingerprint, lastFour);
    }

    public static EmployeeNationalIdentity create(
            EmployeeNationalIdentityId id,
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType,
            String encryptedNumber,
            String numberFingerprint,
            String lastFour
    ) {
        return new EmployeeNationalIdentity(
                id,
                employeeId,
                countryCode,
                identityType,
                encryptedNumber,
                numberFingerprint,
                lastFour
        );
    }

    public static EmployeeNationalIdentity rehydrate(
            EmployeeNationalIdentityId id,
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType,
            String encryptedNumber,
            String numberFingerprint,
            String lastFour
    ) {
        return new EmployeeNationalIdentity(
                id,
                employeeId,
                countryCode,
                identityType,
                encryptedNumber,
                numberFingerprint,
                lastFour
        );
    }

    public void updateProtectedNumber(
            String encryptedNumber,
            String numberFingerprint,
            String lastFour
    ) {
        this.encryptedNumber = requireText(encryptedNumber, "encryptedNumber");
        this.numberFingerprint = requireText(numberFingerprint, "numberFingerprint");
        this.lastFour = requireLastFour(lastFour);
    }

    public EmployeeNationalIdentityId getId() {
        return id;
    }

    public EmployeeId getEmployeeId() {
        return employeeId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public NationalIdentityType getIdentityType() {
        return identityType;
    }

    public String getEncryptedNumber() {
        return encryptedNumber;
    }

    public String getNumberFingerprint() {
        return numberFingerprint;
    }

    public String getLastFour() {
        return lastFour;
    }

    public String getMaskedNumber() {
        return "********" + lastFour;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String requireLastFour(String value) {
        if (value == null || !value.matches("^[0-9]{4}$")) {
            throw new IllegalArgumentException("lastFour must contain exactly 4 digits");
        }
        return value;
    }
}