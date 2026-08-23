package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface EmployeeJpaRepository extends BaseJpaRepository<EmployeeJpaEntity, UUID> {

  Optional<EmployeeJpaEntity> findByEmployeeCode(String employeeCode);

  boolean existsByEmployeeCode(String employeeCode);

  @Query(
"""
 select e from EmployeeJpaEntity  e where (:status is null or e.status=:status)
""")
  Set<EmployeeJpaEntity> findAllByEmployeeStatus(@Param("status") EmployeeStatus employeeStatus);

  @Query(
"""
    SELECT e
    FROM EmployeeJpaEntity e
    WHERE (:employeeId IS NULL OR e.id = :employeeId)
      AND (:employeeCode IS NULL OR e.employeeCode = :employeeCode)
      AND (:employeeStatus IS NULL OR e.status = :employeeStatus)
""")
  List<EmployeeJpaEntity> findByAllEmployeeIdAndEmployeeCode(
      @Nullable @Param("employeeId") UUID employeeId,
      @Nullable @Param("employeeCode") String employeeCode,
      @Nullable @Param("employeeStatus") EmployeeStatus employeeStatus);
}
