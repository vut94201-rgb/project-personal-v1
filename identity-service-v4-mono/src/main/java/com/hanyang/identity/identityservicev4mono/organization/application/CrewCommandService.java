package com.hanyang.identity.identityservicev4mono.organization.application;


import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateCrewCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.command.UpdateCrewCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.*;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class CrewCommandService {
    private final CrewRepository crewRepository;
    private final DepartmentRepository departmentRepository;
    private final OrganizationalAssignmentRepository assignmentRepository;

    @Transactional
    public Crew create(CreateCrewCommand command) {
        Department department = departmentRepository.findById(command.departmentId())
                .orElseThrow(() -> new DepartmentNotFoundException(command.departmentId()));
        if (department.getStatus() != OrganizationReferenceStatus.ACTIVE) {
            throw new OrganizationReferenceDisabledException(
                    "Department",
                    command.departmentId().value()
            );
        }

        Crew crew = Crew.create(CrewId.newId(), command.departmentId(), command.code(), command.name());
        if (crewRepository.existsByDepartmentIdAndCode(command.departmentId(), crew.getCode())) {
            throw new CrewCodeAlreadyExistsException(command.departmentId(), crew.getCode());
        }
        return crewRepository.save(crew);
    }

    @Transactional
    public Crew update(UpdateCrewCommand command) {
        Crew crew = crewRepository.findById(command.crewId())
                .orElseThrow(() -> new CrewNotFoundException(command.crewId()));
        crew.rename(command.name());
        return crewRepository.save(crew);
    }

    @Transactional
    public void disable(CrewId crewId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new CrewNotFoundException(crewId));
        if (assignmentRepository.existsActiveByCrewId(crewId)) {
            throw new CrewHasActiveAssignmentsException(crewId);
        }
        crew.disable();
        crewRepository.save(crew);
    }
}