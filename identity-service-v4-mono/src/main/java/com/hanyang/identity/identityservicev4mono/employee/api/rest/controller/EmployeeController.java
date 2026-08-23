package com.hanyang.identity.identityservicev4mono.employee.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.employee.api.mapper.EmployeeRestMapper;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.request.CreateEmployeeRequest;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.request.UpdateEmployeeRequest;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.response.EmployeeResponse;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeCommandService;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeQueryService;
import com.hanyang.identity.identityservicev4mono.employee.application.command.UpdateEmployeeCommand;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeCommandService commandService;
  private final EmployeeQueryService queryService;
  private final EmployeeRestMapper mapper;

  @PostMapping
  public ResponseEntity<EmployeeResponse> create(
      @Valid @RequestBody CreateEmployeeRequest request) {
    Employee employee = commandService.create(mapper.toCommand(request));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(employee));
  }

  @GetMapping("/{id}")
  public EmployeeResponse getById(@PathVariable UUID id) {
    Employee employee = queryService.getById(new EmployeeId(id));

    return mapper.toResponse(employee);
  }

  @PutMapping("/{id}")
  public EmployeeResponse update(
      @PathVariable UUID id, @Valid @RequestBody UpdateEmployeeRequest request) {
    Employee employee =
        commandService.update(new UpdateEmployeeCommand(new EmployeeId(id), request.fullName()));

    return mapper.toResponse(employee);
  }

  @PatchMapping("/{id}/terminate")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void terminate(@PathVariable UUID id) {
    commandService.terminate(new EmployeeId(id));
  }

  @GetMapping("/find-id-code-status")
  public List<EmployeeResponse> findByIdAndEmployeeCodeAndStatus(
      @RequestParam(required = false) String id,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String status) {

    EmployeeStatus employeeStatus;
    if (StringUtils.hasText(status)) {
      employeeStatus = EmployeeStatus.valueOf(status);
    } else {
      employeeStatus = null;
    }
    var result = queryService.findByIdAndCodeAndStatus(id, code, employeeStatus);
    return result.stream().map(mapper::toResponse).toList();
  }

  @GetMapping("/find-by-status")
  public List<Employee> findAllByEmployeeStatus(
      @RequestParam(required = false, name = "status") String employeeStatus) {

    if (Objects.nonNull(employeeStatus) && StringUtils.hasText(employeeStatus)) {
      return queryService.findAllByEmployeeStatus(EmployeeStatus.valueOf(employeeStatus));

    } else {
      return queryService.findAllByEmployeeStatus(null);
    }
  }
}
