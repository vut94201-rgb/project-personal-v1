package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AccountPersistenceMapper {

    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "employeeId", source = "employeeId.value")
    AccountJpaEntity toEntity(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "username", ignore = true)
    void updateEntity(
            Account account,
            @MappingTarget AccountJpaEntity entity
    );

    default Account toDomain(AccountJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Account.rehydrate(
                new AccountId(entity.getId()),
                new EmployeeId(entity.getEmployeeId()),
                entity.getUsername(),
                entity.getKeycloakSubject(),
                entity.getStatus()
        );
    }
}