package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface EmployeePersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    EmployeeJpaEntity toEntity(Employee employee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    void updateEntity(
            Employee employee,
            @MappingTarget EmployeeJpaEntity entity
    );

    default Employee toDomain(EmployeeJpaEntity entity) {
        return Employee.rehydrate(
                new EmployeeId(entity.getId()),
                entity.getEmployeeCode(),
                entity.getFullName(),
                entity.getStatus()
        );
    }
}