package com.hanyang.identity.identityservicev4mono.shared.exception;


import com.hanyang.identity.identityservicev4mono.access.application.exception.*;
import com.hanyang.identity.identityservicev4mono.account.application.exception.*;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.*;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakUserConflictException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.*;
import com.hanyang.identity.identityservicev4mono.shared.outbox.exception.OutboxEventNotFoundException;
import com.hanyang.identity.identityservicev4mono.shared.outbox.exception.OutboxEventRetryNotAllowedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Clock clock;

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "EMPLOYEE_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(EmployeeCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeCodeAlreadyExists(
            EmployeeCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "EMPLOYEE_CODE_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(EmployeeProfileNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeProfileNotFound(
            EmployeeProfileNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "EMPLOYEE_PROFILE_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(EmployeeEmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeEmailAlreadyExists(
            EmployeeEmailAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "EMPLOYEE_EMAIL_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }


    @ExceptionHandler(NationalIdentityAlreadyAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleNationalIdentityAlreadyAssigned(
            NationalIdentityAlreadyAssignedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "NATIONAL_IDENTITY_ALREADY_ASSIGNED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }




    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentNotFound(
            DepartmentNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "DEPARTMENT_NOT_FOUND", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(DepartmentCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentCodeAlreadyExists(
            DepartmentCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "DEPARTMENT_CODE_ALREADY_EXISTS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(DepartmentHasActiveAssignmentsException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentHasActiveAssignments(
            DepartmentHasActiveAssignmentsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "DEPARTMENT_HAS_ACTIVE_ASSIGNMENTS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(DepartmentHasActiveCrewsException.class)
    public ResponseEntity<ApiErrorResponse> handleDepartmentHasActiveCrews(
            DepartmentHasActiveCrewsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "DEPARTMENT_HAS_ACTIVE_CREWS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(CrewNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleCrewNotFound(
            CrewNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "CREW_NOT_FOUND", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(CrewCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleCrewCodeAlreadyExists(
            CrewCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "CREW_CODE_ALREADY_EXISTS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(CrewHasActiveAssignmentsException.class)
    public ResponseEntity<ApiErrorResponse> handleCrewHasActiveAssignments(
            CrewHasActiveAssignmentsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "CREW_HAS_ACTIVE_ASSIGNMENTS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(CrewDepartmentMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleCrewDepartmentMismatch(
            CrewDepartmentMismatchException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "CREW_DEPARTMENT_MISMATCH", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(PositionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePositionNotFound(
            PositionNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "POSITION_NOT_FOUND", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(PositionCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handlePositionCodeAlreadyExists(
            PositionCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "POSITION_CODE_ALREADY_EXISTS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(PositionHasActiveAssignmentsException.class)
    public ResponseEntity<ApiErrorResponse> handlePositionHasActiveAssignments(
            PositionHasActiveAssignmentsException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "POSITION_HAS_ACTIVE_ASSIGNMENTS", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(OrganizationalAssignmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrganizationalAssignmentNotFound(
            OrganizationalAssignmentNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.NOT_FOUND, "ORGANIZATIONAL_ASSIGNMENT_NOT_FOUND", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(EmployeeAlreadyHasActiveOrganizationalAssignmentException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeAlreadyHasActiveOrganizationalAssignment(
            EmployeeAlreadyHasActiveOrganizationalAssignmentException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "EMPLOYEE_ALREADY_HAS_ACTIVE_ORGANIZATIONAL_ASSIGNMENT",
                exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(OrganizationReferenceDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleOrganizationReferenceDisabled(
            OrganizationReferenceDisabledException exception,
            HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "ORGANIZATION_REFERENCE_DISABLED", exception.getMessage(),
                request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotFound(
            AccountNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "ACCOUNT_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "USERNAME_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(EmployeeAlreadyHasAccountException.class)
    public ResponseEntity<ApiErrorResponse> handleEmployeeAlreadyHasAccount(
            EmployeeAlreadyHasAccountException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "EMPLOYEE_ALREADY_HAS_ACCOUNT",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountDisabled(
            AccountDisabledException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ACCOUNT_DISABLED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccountNotProvisionedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountNotProvisioned(
            AccountNotProvisionedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ACCOUNT_NOT_PROVISIONED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccountRoleAlreadyAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountRoleAlreadyAssigned(
            AccountRoleAlreadyAssignedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ACCOUNT_ROLE_ALREADY_ASSIGNED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccountRoleNotAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountRoleNotAssigned(
            AccountRoleNotAssignedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "ACCOUNT_ROLE_NOT_ASSIGNED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleApplicationNotFound(
            ApplicationNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "APPLICATION_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ApplicationCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleApplicationCodeAlreadyExists(
            ApplicationCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "APPLICATION_CODE_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(ApplicationDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleApplicationDisabled(
            ApplicationDisabledException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "APPLICATION_DISABLED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFound(
            RoleNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "ROLE_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(RoleCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleCodeAlreadyExists(
            RoleCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ROLE_CODE_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissionNotFound(
            PermissionNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "PERMISSION_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(PermissionCodeAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissionCodeAlreadyExists(
            PermissionCodeAlreadyExistsException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "PERMISSION_CODE_ALREADY_EXISTS",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(RoleDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleDisabled(
            RoleDisabledException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ROLE_DISABLED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(PermissionDisabledException.class)
    public ResponseEntity<ApiErrorResponse> handlePermissionDisabled(
            PermissionDisabledException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "PERMISSION_DISABLED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(RolePermissionAlreadyAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleRolePermissionAlreadyAssigned(
            RolePermissionAlreadyAssignedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ROLE_PERMISSION_ALREADY_ASSIGNED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(RolePermissionNotAssignedException.class)
    public ResponseEntity<ApiErrorResponse> handleRolePermissionNotAssigned(
            RolePermissionNotAssignedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "ROLE_PERMISSION_NOT_ASSIGNED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(RolePermissionApplicationMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleRolePermissionApplicationMismatch(
            RolePermissionApplicationMismatchException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ROLE_PERMISSION_APPLICATION_MISMATCH",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccountProvisioningNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountProvisioningNotAllowed(
            AccountProvisioningNotAllowedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ACCOUNT_PROVISIONING_NOT_ALLOWED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccountCredentialOnboardingNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountCredentialOnboardingNotAllowed(
            AccountCredentialOnboardingNotAllowedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "ACCOUNT_CREDENTIAL_ONBOARDING_NOT_ALLOWED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OutboxEventNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOutboxEventNotFound(
            OutboxEventNotFoundException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                "OUTBOX_EVENT_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OutboxEventRetryNotAllowedException.class)
    public ResponseEntity<ApiErrorResponse> handleOutboxEventRetryNotAllowed(
            OutboxEventRetryNotAllowedException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "OUTBOX_EVENT_RETRY_NOT_ALLOWED",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(KeycloakUserConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleKeycloakUserConflict(
            KeycloakUserConflictException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.CONFLICT,
                "KEYCLOAK_USER_CONFLICT",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(KeycloakIntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleKeycloakIntegration(
            KeycloakIntegrationException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.BAD_GATEWAY,
                "KEYCLOAK_INTEGRATION_ERROR",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage()
                )
        );

        return build(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_ERROR",
                "Request validation failed",
                request.getRequestURI(),
                Map.copyOf(fieldErrors)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                exception.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String code,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(clock),
                status.value(),
                code,
                message,
                path,
                fieldErrors
        );

        return ResponseEntity.status(status).body(response);
    }
}