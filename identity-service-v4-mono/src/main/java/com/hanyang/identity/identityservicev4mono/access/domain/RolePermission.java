package com.hanyang.identity.identityservicev4mono.access.domain;


import lombok.Getter;

import java.util.Objects;
@Getter
public class RolePermission {

    private final RoleId roleId;
    private final PermissionId permissionId;

    private RolePermission(
            RoleId roleId,
            PermissionId permissionId
    ) {
        this.roleId = Objects.requireNonNull(
                roleId,
                "roleId must not be null"
        );
        this.permissionId = Objects.requireNonNull(
                permissionId,
                "permissionId must not be null"
        );
    }

    public static RolePermission create(
            RoleId roleId,
            PermissionId permissionId
    ) {
        return new RolePermission(roleId, permissionId);
    }

    public static RolePermission rehydrate(
            RoleId roleId,
            PermissionId permissionId
    ) {
        return new RolePermission(roleId, permissionId);
    }
}