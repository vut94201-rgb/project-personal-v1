package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeeProfilePersistenceMapper {

    @Mapping(target = "employeeId", source = "employeeId.value")
    EmployeeProfileJpaEntity toEntity(EmployeeProfile profile);

    @Mapping(target = "employeeId", ignore = true)
    void updateEntity(
            EmployeeProfile profile,
            @MappingTarget EmployeeProfileJpaEntity entity
    );

    default EmployeeProfile toDomain(EmployeeProfileJpaEntity entity) {
        return EmployeeProfile.rehydrate(
                new EmployeeId(entity.getEmployeeId()),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getAddress(),
                entity.getHireDate()
        );
    }
}