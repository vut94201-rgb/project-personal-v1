package com.hanyang.identity.identityservicev4mono.organization.domain;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository {
    Department save(Department department);
    Optional<Department> findById(DepartmentId id);
    Optional<Department> findByCode(String code);
    boolean existsByCode(String code);
    List<Department> findAllByStatus(@Nullable OrganizationReferenceStatus status);
}
