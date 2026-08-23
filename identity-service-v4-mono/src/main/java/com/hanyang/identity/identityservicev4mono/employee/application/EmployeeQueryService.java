package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryService {

  private final EmployeeRepository employeeRepository;

  public Employee getById(EmployeeId id) {
    return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
  }

  public Employee getByEmployeeCode(String code) {
    return employeeRepository
        .findByEmployeeCode(code)
        .orElseThrow(() -> new EmployeeNotFoundException(code));
  }

  public List<Employee> findByIdAndCodeAndStatus(
      String employeeId, String employeeCode, EmployeeStatus employeeStatus) {
    String code = StringUtils.hasText(employeeCode) ? employeeCode : null;
    UUID id = StringUtils.hasText(employeeId) ? UUID.fromString(employeeId) : null;
    return employeeRepository.findByEmployeeIdAndEmployeeCode(id, code, employeeStatus);
  }

  public List<Employee> findAllByEmployeeStatus(@Nullable EmployeeStatus employeeStatus) {

    return employeeRepository.findAllByEmployeeStatus(employeeStatus);
  }
}
