package com.hanyang.identity.identityservicev4mono.organization.api.rest.response;

import java.util.UUID;

public record CrewResponse(
        UUID id,
        UUID departmentId,
        String code,
        String name,
        String status
) {
}