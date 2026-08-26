    package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence;


    import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

    import java.util.Optional;
    import java.util.UUID;

    public interface AccountJpaRepository
            extends BaseJpaRepository<AccountJpaEntity, UUID> {

        Optional<AccountJpaEntity> findByUsername(String username);

        Optional<AccountJpaEntity> findByEmployeeId(UUID employeeId);

        boolean existsByUsername(String username);

        boolean existsByEmployeeId(UUID employeeId);
    }