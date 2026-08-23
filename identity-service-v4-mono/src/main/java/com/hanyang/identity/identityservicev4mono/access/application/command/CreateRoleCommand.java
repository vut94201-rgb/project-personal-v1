package com.hanyang.identity.identityservicev4mono.access.application.command;


import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;

public record CreateRoleCommand(
        ApplicationId applicationId,
        String code,
        String name
) {
}