package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentity;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeNationalIdentityRepositoryAdapter implements EmployeeNationalIdentityRepository {

    private final EmployeeNationalIdentityJpaRepository jpaRepository;
    private final EmployeeNationalIdentityPersistenceMapper mapper;

    @Override
    public EmployeeNationalIdentity save(EmployeeNationalIdentity identity) {
        return jpaRepository.findById(identity.getId().value())
                .map(existing -> {
                    mapper.updateEntity(identity, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> mapper.toDomain(jpaRepository.save(mapper.toEntity(identity))));
    }

    @Override
    public Optional<EmployeeNationalIdentity> findByEmployeeIdAndType(
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType
    ) {
        return jpaRepository.findByEmployeeIdAndCountryCodeAndIdentityType(
                        employeeId.value(),
                        countryCode,
                        identityType
                )
                .map(mapper::toDomain);
    }

    @Override
    public Optional<EmployeeNationalIdentity> findByFingerprint(
            String countryCode,
            NationalIdentityType identityType,
            String fingerprint
    ) {
        return jpaRepository.findByCountryCodeAndIdentityTypeAndNumberFingerprint(
                        countryCode,
                        identityType,
                        fingerprint
                )
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByFingerprintAndEmployeeIdNot(
            String countryCode,
            NationalIdentityType identityType,
            String fingerprint,
            EmployeeId employeeId
    ) {
        return jpaRepository.existsByCountryCodeAndIdentityTypeAndNumberFingerprintAndEmployeeIdNot(
                countryCode,
                identityType,
                fingerprint,
                employeeId.value()
        );
    }
}