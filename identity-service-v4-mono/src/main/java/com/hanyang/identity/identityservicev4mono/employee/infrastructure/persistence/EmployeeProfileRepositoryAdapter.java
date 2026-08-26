package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfile;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmployeeProfileRepositoryAdapter implements EmployeeProfileRepository {

    private final EmployeeProfileJpaRepository jpaRepository;
    private final EmployeeProfilePersistenceMapper mapper;

    @Override
    public EmployeeProfile save(EmployeeProfile profile) {
        return jpaRepository.findById(profile.getEmployeeId().value())
                .map(existing -> {
                    mapper.updateEntity(profile, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() ->
                        mapper.toDomain(jpaRepository.save(mapper.toEntity(profile)))
                );
    }

    @Override
    public Optional<EmployeeProfile> findByEmployeeId(EmployeeId employeeId) {
        return jpaRepository.findById(employeeId.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndEmployeeIdNot(
            String email,
            EmployeeId employeeId
    ) {
        return jpaRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(
                email,
                employeeId.value()
        );
    }
}