package com.hanyang.identity.identityservicev4mono.organization.application.command;

import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;

public record UpdateDepartmentCommand(DepartmentId departmentId, String name) {
}
