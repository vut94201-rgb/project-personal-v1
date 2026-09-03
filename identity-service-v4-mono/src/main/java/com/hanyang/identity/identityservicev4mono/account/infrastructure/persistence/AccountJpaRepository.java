package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends BaseJpaRepository<AccountJpaEntity, UUID> {

  Optional<AccountJpaEntity> findByUsername(String username);

  Optional<AccountJpaEntity> findByEmployeeId(UUID employeeId);

  boolean existsByUsername(String username);

  boolean existsByEmployeeId(UUID employeeId);

  @Query(
      """
         select a from AccountJpaEntity a inner join EmployeeJpaEntity e on e.id=a.employeeId
                 where
                 (:employeeId is not  null and :employeeId=e.id)
                  or
                 (:username is not null  and trim(:username) != '' and :username=a.username)
                  or
                 (:employeeCode is  not null  and trim(:employeeCode) != '' and :employeeCode=e.employeeCode)
        """)
  Optional<AccountJpaEntity> findAccountByEmployeeIdOrUsernameOrEmployeeCode(
      @Nullable @Param("employeeId") UUID employeeId,
      @Nullable @Param("username") String username,
      @Nullable @Param("employeeCode") String employeeCode);

  @Query(
      """
         select  a from AccountJpaEntity  a where :status is null  or :status=a.status
        """)
  List<AccountJpaEntity> findByStatus(@Nullable @Param("status") AccountStatus status);
}
