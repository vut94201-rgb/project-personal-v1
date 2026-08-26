package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationalAssignmentJpaRepository
        extends BaseJpaRepository<OrganizationalAssignmentJpaEntity, UUID> {

    Optional<OrganizationalAssignmentJpaEntity> findByEmployeeIdAndStatus(
            UUID employeeId,
            OrganizationalAssignmentStatus status
    );

    List<OrganizationalAssignmentJpaEntity> findAllByEmployeeIdOrderByEffectiveFromDesc(UUID employeeId);
    List<OrganizationalAssignmentJpaEntity> findAllByDepartmentIdAndStatusOrderByEffectiveFromDesc(
            UUID departmentId, OrganizationalAssignmentStatus status
    );
    List<OrganizationalAssignmentJpaEntity> findAllByPositionIdAndStatusOrderByEffectiveFromDesc(
            UUID positionId, OrganizationalAssignmentStatus status
    );
    List<OrganizationalAssignmentJpaEntity> findAllByCrewIdAndStatusOrderByEffectiveFromDesc(
            UUID crewId, OrganizationalAssignmentStatus status
    );

    boolean existsByEmployeeIdAndStatus(UUID employeeId, OrganizationalAssignmentStatus status);
    boolean existsByDepartmentIdAndStatus(UUID departmentId, OrganizationalAssignmentStatus status);
    boolean existsByPositionIdAndStatus(UUID positionId, OrganizationalAssignmentStatus status);
    boolean existsByCrewIdAndStatus(UUID crewId, OrganizationalAssignmentStatus status);
}