package com.hanyang.identity.identityservicev4mono.employee.domain;


import java.util.Optional;

public interface EmployeeNationalIdentityRepository {

    EmployeeNationalIdentity save(EmployeeNationalIdentity identity);

    Optional<EmployeeNationalIdentity> findByEmployeeIdAndType(
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType
    );

    Optional<EmployeeNationalIdentity> findByFingerprint(
            String countryCode,
            NationalIdentityType identityType,
            String fingerprint
    );

    boolean existsByFingerprintAndEmployeeIdNot(
            String countryCode,
            NationalIdentityType identityType,
            String fingerprint,
            EmployeeId employeeId
    );
}