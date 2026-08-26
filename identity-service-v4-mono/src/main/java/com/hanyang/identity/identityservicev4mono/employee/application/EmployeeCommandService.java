package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.application.command.CreateEmployeeCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.command.UpdateEmployeeCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class EmployeeCommandService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public Employee create(CreateEmployeeCommand command) {

        if (employeeRepository.existsByEmployeeCode(
                command.employeeCode())) {
            throw new EmployeeCodeAlreadyExistsException(
                    command.employeeCode()
            );
        }

        Employee employee = Employee.create(
                EmployeeId.newId(),
                command.employeeCode(),
                command.fullName()
        );

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee update(UpdateEmployeeCommand command) {

        Employee employee =
                employeeRepository.findById(command.employeeId())
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        command.employeeId()
                                )
                        );

        employee.rename(command.fullName());

        return employeeRepository.save(employee);
    }

    @Transactional
    public void terminate(EmployeeId employeeId) {

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(employeeId)
                        );

        employee.terminate();

        employeeRepository.save(employee);
    }
}