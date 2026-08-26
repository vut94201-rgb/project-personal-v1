package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateOrganizationalAssignmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.command.EndOrganizationalAssignmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.*;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class OrganizationalAssignmentCommandService {
    private final OrganizationalAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final CrewRepository crewRepository;
    private final Clock clock;

    @Transactional
    public OrganizationalAssignment assign(CreateOrganizationalAssignmentCommand command) {
        LocalDate today = LocalDate.now(clock);
        if (command.effectiveFrom().isAfter(today)) {
            throw new IllegalArgumentException("effectiveFrom must not be in the future");
        }

        employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        Department department = departmentRepository.findById(command.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(command.departmentId()));
        Position position = positionRepository.findById(command.positionId())
                .orElseThrow(() -> new PositionNotFoundException(command.positionId()));

        if (department.getStatus() != OrganizationReferenceStatus.ACTIVE) {
            throw new OrganizationReferenceDisabledException("Department", command.departmentId().value());
        }
        if (position.getStatus() != OrganizationReferenceStatus.ACTIVE) {
            throw new OrganizationReferenceDisabledException("Position", command.positionId().value());
        }

        if (command.crewId() != null) {
            Crew crew = crewRepository.findById(command.crewId())
                    .orElseThrow(() -> new CrewNotFoundException(command.crewId()));
            if (crew.getStatus() != OrganizationReferenceStatus.ACTIVE) {
                throw new OrganizationReferenceDisabledException("Crew", command.crewId().value());
            }
            if (!crew.getDepartmentId().equals(command.departmentId())) {
                throw new CrewDepartmentMismatchException(command.crewId(), command.departmentId());
            }
        }

        if (assignmentRepository.existsActiveByEmployeeId(command.employeeId())) {
            throw new EmployeeAlreadyHasActiveOrganizationalAssignmentException(command.employeeId());
        }

        OrganizationalAssignment assignment = OrganizationalAssignment.create(
                OrganizationalAssignmentId.newId(),
                command.employeeId(),
                command.departmentId(),
                command.positionId(),
                command.crewId(),
                command.effectiveFrom()
        );
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public OrganizationalAssignment end(EndOrganizationalAssignmentCommand command) {
        LocalDate today = LocalDate.now(clock);
        if (command.effectiveTo().isAfter(today)) {
            throw new IllegalArgumentException("effectiveTo must not be in the future");
        }
        OrganizationalAssignment assignment = assignmentRepository.findById(command.assignmentId())
                .orElseThrow(() -> new OrganizationalAssignmentNotFoundException(command.assignmentId()));
        assignment.end(command.effectiveTo());
        return assignmentRepository.save(assignment);
    }
}