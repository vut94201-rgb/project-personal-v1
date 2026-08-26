package com.hanyang.identity.identityservicev4mono.organization.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

import java.util.List;
import java.util.Optional;

public interface OrganizationalAssignmentRepository {
    OrganizationalAssignment save(OrganizationalAssignment assignment);
    Optional<OrganizationalAssignment> findById(OrganizationalAssignmentId id);
    Optional<OrganizationalAssignment> findActiveByEmployeeId(EmployeeId employeeId);
    List<OrganizationalAssignment> findAllByEmployeeId(EmployeeId employeeId);
    List<OrganizationalAssignment> findAllActiveByDepartmentId(DepartmentId departmentId);
    List<OrganizationalAssignment> findAllActiveByPositionId(PositionId positionId);
    List<OrganizationalAssignment> findAllActiveByCrewId(CrewId crewId);
    boolean existsActiveByEmployeeId(EmployeeId employeeId);
    boolean existsActiveByDepartmentId(DepartmentId departmentId);
    boolean existsActiveByPositionId(PositionId positionId);
    boolean existsActiveByCrewId(CrewId crewId);
}