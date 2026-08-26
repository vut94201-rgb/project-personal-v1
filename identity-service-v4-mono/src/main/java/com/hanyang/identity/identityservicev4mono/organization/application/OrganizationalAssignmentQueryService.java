package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.OrganizationalAssignmentNotFoundException;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@IdentityReadAccess
@Transactional(readOnly = true)
public class OrganizationalAssignmentQueryService {
    private final OrganizationalAssignmentRepository assignmentRepository;

    public OrganizationalAssignment getById(OrganizationalAssignmentId id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new OrganizationalAssignmentNotFoundException(id));
    }

    public Optional<OrganizationalAssignment> findActiveByEmployeeId(EmployeeId employeeId) {
        return assignmentRepository.findActiveByEmployeeId(employeeId);
    }

    public List<OrganizationalAssignment> findHistoryByEmployeeId(EmployeeId employeeId) {
        return assignmentRepository.findAllByEmployeeId(employeeId);
    }

    public List<OrganizationalAssignment> findActiveByDepartmentId(DepartmentId departmentId) {
        return assignmentRepository.findAllActiveByDepartmentId(departmentId);
    }

    public List<OrganizationalAssignment> findActiveByPositionId(PositionId positionId) {
        return assignmentRepository.findAllActiveByPositionId(positionId);
    }

    public List<OrganizationalAssignment> findActiveByCrewId(CrewId crewId) {
        return assignmentRepository.findAllActiveByCrewId(crewId);
    }
}