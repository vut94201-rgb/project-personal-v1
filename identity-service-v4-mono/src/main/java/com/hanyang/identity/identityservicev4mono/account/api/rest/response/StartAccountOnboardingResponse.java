package com.hanyang.identity.identityservicev4mono.account.api.rest.response;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

public record StartAccountOnboardingResponse(
        UUID accountId,
        UUID employeeId,
        String employeeCode,
        UUID nationalIdentityId,
        String maskedNationalIdentity,
        String username,
        AccountStatus accountStatus
) {
}