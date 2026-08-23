package com.hanyang.identity.identityservicev4mono.access.api.rest.response;

import java.util.UUID;

public record ApplicationResponse(
        UUID id,
        String code,
        String name,
        String status
) {
}