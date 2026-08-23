package com.hanyang.identity.identityservicev4mono.access.domain;

import java.util.List;

public interface RolePermissionRepository {

    RolePermission save(RolePermission rolePermission);

            void delete(RoleId roleId, PermissionId permissionId);

            boolean exists(RoleId roleId, PermissionId permissionId);

            List<PermissionId> findPermissionIdsByRoleId(RoleId roleId);
}