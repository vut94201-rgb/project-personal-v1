package com.hanyang.identity.identityservicev4mono.access.application.exception;

import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;

public class RolePermissionApplicationMismatchException extends RuntimeException {

           public RolePermissionApplicationMismatchException(
            RoleId roleId,
            PermissionId permissionId
    ) {
                super(
                                "Role " + roleId.value()
                                                + " and permission " + permissionId.value()
                                                + " belong to different applications"
                                );
           }
}