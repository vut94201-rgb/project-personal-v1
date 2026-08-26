package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface OrganizationalAssignmentPersistenceMapper {
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "employeeId", source = "employeeId.value")
    @Mapping(target = "departmentId", source = "departmentId.value")
    @Mapping(target = "positionId", source = "positionId.value")
    @Mapping(target = "crewId", source = "crewId.value")
    OrganizationalAssignmentJpaEntity toEntity(OrganizationalAssignment assignment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "positionId", ignore = true)
    @Mapping(target = "crewId", ignore = true)
    @Mapping(target = "effectiveFrom", ignore = true)
    void updateEntity(
            OrganizationalAssignment assignment,
            @MappingTarget OrganizationalAssignmentJpaEntity entity
    );

    default OrganizationalAssignment toDomain(OrganizationalAssignmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return OrganizationalAssignment.rehydrate(
                new OrganizationalAssignmentId(entity.getId()),
                new EmployeeId(entity.getEmployeeId()),
                new DepartmentId(entity.getDepartmentId()),
                new PositionId(entity.getPositionId()),
                entity.getCrewId() == null ? null : new CrewId(entity.getCrewId()),
                entity.getEffectiveFrom(),
                entity.getEffectiveTo(),
                entity.getStatus()
        );
    }
}