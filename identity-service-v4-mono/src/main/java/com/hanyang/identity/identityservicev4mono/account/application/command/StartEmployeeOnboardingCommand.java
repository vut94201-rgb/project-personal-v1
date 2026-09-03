package com.hanyang.identity.identityservicev4mono.account.application.command;


import java.time.LocalDate;

/**
 * Application input for onboarding one employee/account identity set.
 *
 * <p>The application service will own generated identifiers/codes and the V1 national-identity
 * policy. Keeping these values out of the command prevents REST/CSV adapters from becoming the
 * source of truth for employeeCode, country code, identity type, or Account status.</p>
 */
public record StartEmployeeOnboardingCommand(
        String fullName,
        String username,
        String nationalIdentityNumber,
        String email,
        String phone,
        String address,
        LocalDate joinDate
) {
}