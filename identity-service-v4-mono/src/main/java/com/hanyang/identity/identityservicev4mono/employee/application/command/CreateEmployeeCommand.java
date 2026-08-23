package com.hanyang.identity.identityservicev4mono.employee.application.command;
public record CreateEmployeeCommand(
        String employeeCode,
        String fullName
) {
}