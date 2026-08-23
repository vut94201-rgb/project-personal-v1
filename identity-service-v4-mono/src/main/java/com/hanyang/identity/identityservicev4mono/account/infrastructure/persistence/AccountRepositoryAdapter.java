package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter
        implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountPersistenceMapper mapper;

    @Override
    public Account save(Account account) {

        return jpaRepository.findById(account.getId().value())
                .map(existing -> {
                    mapper.updateEntity(account, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> {
                    AccountJpaEntity entity =
                            mapper.toEntity(account);

                    return mapper.toDomain(
                            jpaRepository.save(entity)
                    );
                });
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmployeeId(
            EmployeeId employeeId
    ) {
        return jpaRepository
                .findByEmployeeId(employeeId.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmployeeId(EmployeeId employeeId) {
        return jpaRepository.existsByEmployeeId(
                employeeId.value()
        );
    }
}