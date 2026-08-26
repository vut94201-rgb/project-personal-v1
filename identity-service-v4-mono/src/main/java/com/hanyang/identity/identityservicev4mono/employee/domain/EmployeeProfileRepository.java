package com.hanyang.identity.identityservicev4mono.employee.domain;

import java.util.Optional;

public interface EmployeeProfileRepository {

    EmployeeProfile save(EmployeeProfile profile);

    Optional<EmployeeProfile> findByEmployeeId(EmployeeId employeeId);

    boolean existsByEmailIgnoreCaseAndEmployeeIdNot(
            String email,
            EmployeeId employeeId
    );
}