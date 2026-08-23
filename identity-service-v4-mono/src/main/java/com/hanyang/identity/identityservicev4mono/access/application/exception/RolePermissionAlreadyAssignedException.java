package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;

public class RolePermissionAlreadyAssignedException extends RuntimeException {

    public RolePermissionAlreadyAssignedException(
         RoleId roleId,
            PermissionId permissionId
    ) {        super(
                                "Permission " + permissionId.value()
                                               + " is already assigned to role "
                                                + roleId.value()
                                );
            }
}