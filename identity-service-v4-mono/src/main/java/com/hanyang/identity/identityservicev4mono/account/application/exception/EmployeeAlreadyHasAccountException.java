package com.hanyang.identity.identityservicev4mono.account.application.exception;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

public class EmployeeAlreadyHasAccountException
        extends RuntimeException {

    public EmployeeAlreadyHasAccountException(EmployeeId employeeId) {
        super(
                "Employee already has account: "
                        + employeeId.value()
        );
    }
}