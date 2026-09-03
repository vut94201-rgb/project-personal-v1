package com.hanyang.identity.identityservicev4mono.shared.exception;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.NationalIdentityAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerNationalIdentityTest {

    @Test
    void mapsDuplicateNationalIdentityToConflict() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-31T15:00:00Z"),
                ZoneOffset.UTC
        );
        GlobalExceptionHandler handler = new GlobalExceptionHandler(clock);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/account-onboardings");

        var response = handler.handleNationalIdentityAlreadyAssigned(
                new NationalIdentityAlreadyAssignedException(
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD
                ),
                request
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("NATIONAL_IDENTITY_ALREADY_ASSIGNED", response.getBody().code());
        assertEquals("/api/v1/account-onboardings", response.getBody().path());
    }
}