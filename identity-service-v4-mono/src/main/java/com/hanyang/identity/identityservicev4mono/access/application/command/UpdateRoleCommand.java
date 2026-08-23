package com.hanyang.identity.identityservicev4mono.access.application.command;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;

public record UpdateRoleCommand(
        RoleId roleId,
        String name
) {
}