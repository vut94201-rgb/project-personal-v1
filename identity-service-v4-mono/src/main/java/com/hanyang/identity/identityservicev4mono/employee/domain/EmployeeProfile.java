package com.hanyang.identity.identityservicev4mono.employee.domain;


import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

public class EmployeeProfile {

    private final EmployeeId employeeId;

    private String email;
    private String phoneNumber;
    private String address;
    private LocalDate hireDate;

    private EmployeeProfile(
            EmployeeId employeeId,
            String email,
            String phoneNumber,
            String address,
            LocalDate hireDate
    ) {
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.email = normalizeEmail(email);
        this.phoneNumber = normalizeOptionalText(phoneNumber);
        this.address = normalizeOptionalText(address);
        this.hireDate = hireDate;
    }

    public static EmployeeProfile create(
            EmployeeId employeeId,
            String email,
            String phoneNumber,
            String address,
            LocalDate hireDate
    ) {
        return new EmployeeProfile(employeeId, email, phoneNumber, address, hireDate);
    }

    public static EmployeeProfile rehydrate(
            EmployeeId employeeId,
            String email,
            String phoneNumber,
            String address,
            LocalDate hireDate
    ) {
        return new EmployeeProfile(employeeId, email, phoneNumber, address, hireDate);
    }

    public void update(
            String email,
            String phoneNumber,
            String address,
            LocalDate hireDate
    ) {
        this.email = normalizeEmail(email);
        this.phoneNumber = normalizeOptionalText(phoneNumber);
        this.address = normalizeOptionalText(address);
        this.hireDate = hireDate;
    }

    public EmployeeId getEmployeeId() {
        return employeeId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    private static String normalizeEmail(String value) {
        String normalized = normalizeOptionalText(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private static String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}