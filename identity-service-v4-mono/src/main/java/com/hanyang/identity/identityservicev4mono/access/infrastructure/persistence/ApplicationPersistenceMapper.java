package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ApplicationPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    ApplicationJpaEntity toEntity(Application application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(
            Application application,
            @MappingTarget ApplicationJpaEntity entity
    );

    default Application toDomain(ApplicationJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Application.rehydrate(
                new ApplicationId(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getStatus()
        );
    }
}