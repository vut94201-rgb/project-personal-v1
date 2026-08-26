package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrganizationalAssignmentRepositoryAdapter implements OrganizationalAssignmentRepository {
    private final OrganizationalAssignmentJpaRepository jpaRepository;
    private final OrganizationalAssignmentPersistenceMapper mapper;

    @Override
    public OrganizationalAssignment save(OrganizationalAssignment assignment) {
        UUID id = assignment.getId().value();
        return jpaRepository.findById(id)
                .map(existing -> {
                    mapper.updateEntity(assignment, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> mapper.toDomain(jpaRepository.save(mapper.toEntity(assignment))));
    }

    @Override
    public Optional<OrganizationalAssignment> findById(OrganizationalAssignmentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<OrganizationalAssignment> findActiveByEmployeeId(EmployeeId employeeId) {
        return jpaRepository.findByEmployeeIdAndStatus(
                employeeId.value(), OrganizationalAssignmentStatus.ACTIVE
        ).map(mapper::toDomain);
    }

    @Override
    public List<OrganizationalAssignment> findAllByEmployeeId(EmployeeId employeeId) {
        return jpaRepository.findAllByEmployeeIdOrderByEffectiveFromDesc(employeeId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrganizationalAssignment> findAllActiveByDepartmentId(DepartmentId departmentId) {
        return jpaRepository.findAllByDepartmentIdAndStatusOrderByEffectiveFromDesc(
                departmentId.value(), OrganizationalAssignmentStatus.ACTIVE
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrganizationalAssignment> findAllActiveByPositionId(PositionId positionId) {
        return jpaRepository.findAllByPositionIdAndStatusOrderByEffectiveFromDesc(
                positionId.value(), OrganizationalAssignmentStatus.ACTIVE
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrganizationalAssignment> findAllActiveByCrewId(CrewId crewId) {
        return jpaRepository.findAllByCrewIdAndStatusOrderByEffectiveFromDesc(
                crewId.value(), OrganizationalAssignmentStatus.ACTIVE
        ).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsActiveByEmployeeId(EmployeeId employeeId) {
        return jpaRepository.existsByEmployeeIdAndStatus(
                employeeId.value(), OrganizationalAssignmentStatus.ACTIVE
        );
    }

    @Override
    public boolean existsActiveByDepartmentId(DepartmentId departmentId) {
        return jpaRepository.existsByDepartmentIdAndStatus(
                departmentId.value(), OrganizationalAssignmentStatus.ACTIVE
        );
    }

    @Override
    public boolean existsActiveByPositionId(PositionId positionId) {
        return jpaRepository.existsByPositionIdAndStatus(
                positionId.value(), OrganizationalAssignmentStatus.ACTIVE
        );
    }

    @Override
    public boolean existsActiveByCrewId(CrewId crewId) {
        return jpaRepository.existsByCrewIdAndStatus(
                crewId.value(), OrganizationalAssignmentStatus.ACTIVE
        );
    }
}