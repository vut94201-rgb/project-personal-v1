package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RolePersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "applicationId", source = "applicationId.value")
    RoleJpaEntity toEntity(Role role);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(
            Role role,
            @MappingTarget RoleJpaEntity entity
    );

    default Role toDomain(RoleJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Role.rehydrate(
                new RoleId(entity.getId()),
                new ApplicationId(entity.getApplicationId()),
                entity.getCode(),
                entity.getName(),
                entity.getStatus()
        );
    }
}