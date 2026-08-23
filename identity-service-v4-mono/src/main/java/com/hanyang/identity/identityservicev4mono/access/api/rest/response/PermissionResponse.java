package com.hanyang.identity.identityservicev4mono.access.api.rest.response;

import java.util.UUID;

public record PermissionResponse(
        UUID id,
        UUID applicationId,
        String code,
        String name,
        String status
) {
}