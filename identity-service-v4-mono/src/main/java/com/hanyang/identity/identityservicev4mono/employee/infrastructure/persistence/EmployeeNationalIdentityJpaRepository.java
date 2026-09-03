package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeNationalIdentityJpaRepository
        extends BaseJpaRepository<EmployeeNationalIdentityJpaEntity, UUID> {

    Optional<EmployeeNationalIdentityJpaEntity> findByEmployeeIdAndCountryCodeAndIdentityType(
            UUID employeeId,
            String countryCode,
            NationalIdentityType identityType
    );

    Optional<EmployeeNationalIdentityJpaEntity> findByCountryCodeAndIdentityTypeAndNumberFingerprint(
            String countryCode,
            NationalIdentityType identityType,
            String numberFingerprint
    );

    boolean existsByCountryCodeAndIdentityTypeAndNumberFingerprintAndEmployeeIdNot(
            String countryCode,
            NationalIdentityType identityType,
            String numberFingerprint,
            UUID employeeId
    );
}