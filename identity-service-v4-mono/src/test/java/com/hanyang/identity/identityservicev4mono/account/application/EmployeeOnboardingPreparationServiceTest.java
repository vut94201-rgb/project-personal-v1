package com.hanyang.identity.identityservicev4mono.account.application;

import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingPreparation;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingPreparationService;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.NationalIdentityAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.employee.application.port.EmployeeCodeGenerator;
import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentity;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmployeeOnboardingPreparationServiceTest {

    @Test
    void preparesProtectedIdentityAndGeneratedEmployeeCodeForNewCitizenId() {
        EmployeeNationalIdentityRepository nationalIdentityRepository =
                mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort =
                mock(NationalIdentityProtectionPort.class);
        EmployeeCodeGenerator employeeCodeGenerator =
                mock(EmployeeCodeGenerator.class);

        when(protectionPort.fingerprint("001204012345")).thenReturn("fingerprint");
        when(nationalIdentityRepository.findByFingerprint(
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "fingerprint"
        )).thenReturn(Optional.empty());
        when(protectionPort.encrypt("001204012345")).thenReturn("v1:iv:ciphertext");
        when(employeeCodeGenerator.nextCode()).thenReturn("HY000042");

        EmployeeOnboardingPreparationService service =
                new EmployeeOnboardingPreparationService(
                        nationalIdentityRepository,
                        protectionPort,
                        employeeCodeGenerator
                );

        EmployeeOnboardingPreparation result = service.prepare(command(" 001204012345 "));

        assertNotNull(result.employeeId());
        assertEquals("HY000042", result.employeeCode());
        assertEquals("VN", result.countryCode());
        assertEquals(NationalIdentityType.NATIONAL_ID_CARD, result.identityType());
        assertEquals("v1:iv:ciphertext", result.encryptedNumber());
        assertEquals("fingerprint", result.numberFingerprint());
        assertEquals("2345", result.lastFour());

        verify(protectionPort).fingerprint("001204012345");
        verify(protectionPort).encrypt("001204012345");
    }

    @Test
    void rejectsAlreadyRegisteredCitizenIdBeforeGeneratingEmployeeCodeOrEncryptingAgain() {
        EmployeeNationalIdentityRepository nationalIdentityRepository =
                mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort =
                mock(NationalIdentityProtectionPort.class);
        EmployeeCodeGenerator employeeCodeGenerator =
                mock(EmployeeCodeGenerator.class);

        when(protectionPort.fingerprint("001204012345")).thenReturn("fingerprint");
        when(nationalIdentityRepository.findByFingerprint(
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "fingerprint"
        )).thenReturn(Optional.of(mock(EmployeeNationalIdentity.class)));

        EmployeeOnboardingPreparationService service =
                new EmployeeOnboardingPreparationService(
                        nationalIdentityRepository,
                        protectionPort,
                        employeeCodeGenerator
                );

        NationalIdentityAlreadyAssignedException exception = assertThrows(
                NationalIdentityAlreadyAssignedException.class,
                () -> service.prepare(command("001204012345"))
        );

        org.junit.jupiter.api.Assertions.assertFalse(
                exception.getMessage().contains("001204012345")
        );
        verifyNoInteractions(employeeCodeGenerator);
        verify(protectionPort).fingerprint("001204012345");
    }

    @Test
    void rejectsInvalidCitizenIdBeforeProtectionRepositoryLookupAndCodeGeneration() {
        EmployeeNationalIdentityRepository nationalIdentityRepository =
                mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort =
                mock(NationalIdentityProtectionPort.class);
        EmployeeCodeGenerator employeeCodeGenerator =
                mock(EmployeeCodeGenerator.class);

        EmployeeOnboardingPreparationService service =
                new EmployeeOnboardingPreparationService(
                        nationalIdentityRepository,
                        protectionPort,
                        employeeCodeGenerator
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.prepare(command("123"))
        );

        verifyNoInteractions(
                nationalIdentityRepository,
                protectionPort,
                employeeCodeGenerator
        );
    }

    private static StartEmployeeOnboardingCommand command(String nationalIdentityNumber) {
        return new StartEmployeeOnboardingCommand(
                "Nguyen Van A",
                "nguyenvana",
                nationalIdentityNumber,
                "a@example.com",
                "0900000000",
                "Hanoi",
                LocalDate.of(2026, 8, 31)
        );
    }
}