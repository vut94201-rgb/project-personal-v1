package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentity;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeNationalIdentityPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "employeeId", source = "employeeId.value")
    EmployeeNationalIdentityJpaEntity toEntity(EmployeeNationalIdentity identity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "countryCode", ignore = true)
    @Mapping(target = "identityType", ignore = true)
    void updateEntity(
            EmployeeNationalIdentity identity,
            @MappingTarget EmployeeNationalIdentityJpaEntity entity
    );

    default EmployeeNationalIdentity toDomain(EmployeeNationalIdentityJpaEntity entity) {
        return EmployeeNationalIdentity.rehydrate(
                new EmployeeNationalIdentityId(entity.getId()),
                new EmployeeId(entity.getEmployeeId()),
                entity.getCountryCode(),
                entity.getIdentityType(),
                entity.getEncryptedNumber(),
                entity.getNumberFingerprint(),
                entity.getLastFour()
        );
    }
}