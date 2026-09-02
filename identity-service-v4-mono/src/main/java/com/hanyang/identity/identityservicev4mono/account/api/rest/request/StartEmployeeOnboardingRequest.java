package com.hanyang.identity.identityservicev4mono.account.api.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Starts enterprise account onboarding for an Employee that already exists in Identity.
 *
 * <p>This is an Identity-admin workflow request, not a public self-registration contract.
 * Organizational placement and employee identity are managed separately and must not be supplied by
 * the employee through this request.
 */
public record StartAccountOnboardingRequest(
    @NotNull UUID employeeId, @NotBlank @Size(max = 100) String username) {}
