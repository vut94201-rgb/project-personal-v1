package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.UUID;

public interface EmployeeProfileJpaRepository
        extends BaseJpaRepository<EmployeeProfileJpaEntity, UUID> {

    boolean existsByEmailIgnoreCaseAndEmployeeIdNot(
            String email,
            UUID employeeId
    );
}