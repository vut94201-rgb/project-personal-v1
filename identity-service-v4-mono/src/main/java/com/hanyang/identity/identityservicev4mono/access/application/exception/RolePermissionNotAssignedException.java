package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;

public class RolePermissionNotAssignedException extends RuntimeException {
    public RolePermissionNotAssignedException(
            RoleId roleId,
            PermissionId permissionId
    ) {
                super(
                                "Permission " + permissionId.value()
                                                + " is not assigned to role "
                                                + roleId.value()
                               );
            }
}