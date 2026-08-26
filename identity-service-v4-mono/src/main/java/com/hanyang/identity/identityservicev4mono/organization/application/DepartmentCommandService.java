package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateDepartmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.command.UpdateDepartmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.DepartmentCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.DepartmentHasActiveAssignmentsException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.DepartmentHasActiveCrewsException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.DepartmentNotFoundException;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class DepartmentCommandService {
    private final DepartmentRepository departmentRepository;
    private final OrganizationalAssignmentRepository assignmentRepository;
    private final CrewRepository crewRepository;

    @Transactional
    public Department create(CreateDepartmentCommand command) {
        Department department = Department.create(DepartmentId.newId(), command.code(), command.name());
        if (departmentRepository.existsByCode(department.getCode())) {
            throw new DepartmentCodeAlreadyExistsException(department.getCode());
        }
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(UpdateDepartmentCommand command) {
        Department department = departmentRepository.findById(command.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(command.departmentId()));
        department.rename(command.name());
        return departmentRepository.save(department);
    }

    @Transactional
    public void disable(DepartmentId departmentId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
        if (assignmentRepository.existsActiveByDepartmentId(departmentId)) {
            throw new DepartmentHasActiveAssignmentsException(departmentId);
        }
        if (crewRepository.existsActiveByDepartmentId(departmentId)) {
            throw new DepartmentHasActiveCrewsException(departmentId);
        }
        department.disable();
        departmentRepository.save(department);
    }
}