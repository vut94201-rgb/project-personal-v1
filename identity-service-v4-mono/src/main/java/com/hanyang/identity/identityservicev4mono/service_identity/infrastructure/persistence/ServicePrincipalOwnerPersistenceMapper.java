package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwner;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ServicePrincipalOwnerPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "servicePrincipalId", source = "servicePrincipalId.value")
    @Mapping(target = "employeeId", source = "employeeId.value")
    ServicePrincipalOwnerJpaEntity toEntity(ServicePrincipalOwner owner);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "servicePrincipalId", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "ownershipType", ignore = true)
    void updateEntity(
            ServicePrincipalOwner owner,
            @MappingTarget ServicePrincipalOwnerJpaEntity entity
    );

    default ServicePrincipalOwner toDomain(ServicePrincipalOwnerJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return ServicePrincipalOwner.rehydrate(
                new ServicePrincipalOwnerId(entity.getId()),
                new ServicePrincipalId(entity.getServicePrincipalId()),
                new EmployeeId(entity.getEmployeeId()),
                entity.getOwnershipType(),
                entity.getStatus(),
                entity.getRevokedAt()
        );
    }
}