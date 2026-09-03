package com.hanyang.identity.identityservicev4mono.account.domain;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.jspecify.annotations.Nullable;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {

  Account save(Account account);

  Optional<Account> findById(AccountId id);

  Optional<Account> findByUsername(String username);

  Optional<Account> findByEmployeeId(EmployeeId employeeId);

  boolean existsByUsername(String username);

  boolean existsByEmployeeId(EmployeeId employeeId);

  Optional<Account> findAccountByEmployeeIdOrUsernameOrEmployeeCode(
      @Nullable UUID employeeId, @Nullable String username, @Nullable String employeeCode);

}
