package com.hanyang.identity.identityservicev4mono.access.application.command;

import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;

public record UpdatePermissionCommand(
        PermissionId permissionId,
        String name
) {
}