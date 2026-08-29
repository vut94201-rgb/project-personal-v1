package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicePrincipalPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    ServicePrincipalJpaEntity toEntity(ServicePrincipal servicePrincipal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    void updateEntity(
            ServicePrincipal servicePrincipal,
            @MappingTarget ServicePrincipalJpaEntity entity
    );

    default ServicePrincipal toDomain(ServicePrincipalJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ServicePrincipal.rehydrate(
                new ServicePrincipalId(entity.getId()),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getPurpose(),
                entity.getDescription(),
                entity.getStatus()
        );
    }
}