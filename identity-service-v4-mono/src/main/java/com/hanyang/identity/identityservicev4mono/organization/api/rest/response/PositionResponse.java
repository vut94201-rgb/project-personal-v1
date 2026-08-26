package com.hanyang.identity.identityservicev4mono.organization.api.rest.response;

import java.util.UUID;

public record PositionResponse(
        UUID id,
        String code,
        String name,
        String status
) {
}
