package com.hanyang.identity.identityservicev4mono.account.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

import java.util.Optional;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(AccountId id);

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmployeeId(EmployeeId employeeId);

    boolean existsByUsername(String username);

    boolean existsByEmployeeId(EmployeeId employeeId);
}