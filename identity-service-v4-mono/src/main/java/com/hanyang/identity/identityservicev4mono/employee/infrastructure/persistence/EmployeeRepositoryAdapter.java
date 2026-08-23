package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EmployeeRepositoryAdapter implements EmployeeRepository {

  private final EmployeeJpaRepository jpaRepository;
  private final EmployeePersistenceMapper mapper;

  @Override
  public Employee save(Employee employee) {

    UUID id = employee.getId().value();

    return jpaRepository
        .findById(id)
        .map(
            existingEntity -> {
              mapper.updateEntity(employee, existingEntity);

              return mapper.toDomain(existingEntity);
            })
        .orElseGet(
            () -> {
              EmployeeJpaEntity newEntity = mapper.toEntity(employee);

              EmployeeJpaEntity saved = jpaRepository.save(newEntity);

              return mapper.toDomain(saved);
            });
  }

  @Override
  public Optional<Employee> findById(EmployeeId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<Employee> findByEmployeeCode(String code) {
    return jpaRepository.findByEmployeeCode(code).map(mapper::toDomain);
  }

  @Override
  public boolean existsByEmployeeCode(String code) {
    return jpaRepository.existsByEmployeeCode(code);
  }

  @Override
  public List<Employee> findByEmployeeIdAndEmployeeCode(
      UUID employeeId, String employeeCode, EmployeeStatus employeeStatus) {
    return jpaRepository
        .findByAllEmployeeIdAndEmployeeCode(employeeId, employeeCode, employeeStatus)
        .stream()
        .parallel()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Override
  public List<Employee> findAllByEmployeeStatus(@Nullable EmployeeStatus employeeStatus) {
    return jpaRepository.findAllByEmployeeStatus(employeeStatus).stream()
        .map(mapper::toDomain)
        .toList();
  }
}
