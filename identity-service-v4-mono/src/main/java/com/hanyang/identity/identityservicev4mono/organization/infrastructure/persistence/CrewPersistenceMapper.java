package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.Crew;
import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CrewPersistenceMapper {
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "departmentId", source = "departmentId.value")
    CrewJpaEntity toEntity(Crew crew);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "departmentId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(Crew crew, @MappingTarget CrewJpaEntity entity);

    default Crew toDomain(CrewJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Crew.rehydrate(
                new CrewId(entity.getId()),
                new DepartmentId(entity.getDepartmentId()),
                entity.getCode(),
                entity.getName(),
                entity.getStatus()
        );
    }
}