package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Permission;
import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PermissionPersistenceMapper {

            @Mapping(target = "id", source = "id.value")
    @Mapping(target = "applicationId", source = "applicationId.value")
    PermissionJpaEntity toEntity(Permission permission);

            @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(
            Permission permission,
            @MappingTarget PermissionJpaEntity entity
    );

            default Permission toDomain(PermissionJpaEntity entity) {
                if (entity == null) {
                        return null;
                    }

                        return Permission.rehydrate(
                                new PermissionId(entity.getId()),
                                new ApplicationId(entity.getApplicationId()),
                                entity.getCode(),
                                entity.getName(),
                         entity.getStatus()
                                );
            }
}