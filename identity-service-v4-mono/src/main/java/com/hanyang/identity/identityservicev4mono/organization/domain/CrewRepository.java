package com.hanyang.identity.identityservicev4mono.organization.domain;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public interface CrewRepository {
    Crew save(Crew crew);
    Optional<Crew> findById(CrewId id);
    Optional<Crew> findByDepartmentIdAndCode(DepartmentId departmentId, String code);
    boolean existsByDepartmentIdAndCode(DepartmentId departmentId, String code);
    List<Crew> findAllByDepartmentIdAndStatus(
            DepartmentId departmentId,
            @Nullable OrganizationReferenceStatus status
    );
    boolean existsActiveByDepartmentId(DepartmentId departmentId);
}