package com.hanyang.identity.identityservicev4mono.account.api.rest.response;

import java.util.UUID;


public record AccountResponse(
        UUID id,
        UUID employeeId,
        String username,
        String status
) {
}