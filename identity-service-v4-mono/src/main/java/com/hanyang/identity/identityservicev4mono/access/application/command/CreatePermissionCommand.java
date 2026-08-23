package com.hanyang.identity.identityservicev4mono.access.application.command;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;

public record CreatePermissionCommand(
        ApplicationId applicationId,
        String code,
        String name
) {
}