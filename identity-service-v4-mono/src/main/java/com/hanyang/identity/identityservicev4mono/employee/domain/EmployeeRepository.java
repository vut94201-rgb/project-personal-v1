package com.hanyang.identity.identityservicev4mono.employee.domain;


import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {

  Employee save(Employee employee);

  Optional<Employee> findById(EmployeeId id);

  Optional<Employee> findByEmployeeCode(String employeeCode);

  boolean existsByEmployeeCode(String employeeCode);

  List<Employee> findByEmployeeIdAndEmployeeCode(
      @Nullable UUID employeeId, String employeeCode, EmployeeStatus employeeStatus);
  Optional<Employee> findEmployeeByAccountId(@Nullable UUID accountId);
  List<Employee> findAllByEmployeeStatus(EmployeeStatus employeeStatus);
}
