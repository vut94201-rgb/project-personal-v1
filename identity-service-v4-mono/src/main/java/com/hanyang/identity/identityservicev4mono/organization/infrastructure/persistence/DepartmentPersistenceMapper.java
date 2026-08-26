package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.Department;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DepartmentPersistenceMapper {
    @Mapping(target = "id", source = "id.value")
    DepartmentJpaEntity toEntity(Department department);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(Department department, @MappingTarget DepartmentJpaEntity entity);

    default Department toDomain(DepartmentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Department.rehydrate(
                new DepartmentId(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getStatus()
        );
    }
}
