package com.hanyang.identity.identityservicev4mono.employee.domain;

import com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence.EmployeeJpaEntity;
import jakarta.annotation.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  List<Employee> findAllByEmployeeStatus(EmployeeStatus employeeStatus);
}
