package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.application.command.UpsertEmployeeNationalIdentityCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.NationalIdentityAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmployeeNationalIdentityCommandServiceTest {

    @Test
    void createsProtectedNationalIdentityForExistingEmployee() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository identityRepository = mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort = mock(NationalIdentityProtectionPort.class);
        EmployeeId employeeId = EmployeeId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV001", "Operator One")
        ));
        when(protectionPort.fingerprint("001204012345")).thenReturn(
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
        );
        when(protectionPort.encrypt("001204012345")).thenReturn("v1:iv:ciphertext");
        when(identityRepository.existsByFingerprintAndEmployeeIdNot(
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
                employeeId
        )).thenReturn(false);
        when(identityRepository.findByEmployeeIdAndType(
                employeeId,
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD
        )).thenReturn(Optional.empty());
        when(identityRepository.save(any(EmployeeNationalIdentity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeNationalIdentityCommandService service = new EmployeeNationalIdentityCommandService(
                employeeRepository,
                identityRepository,
                protectionPort
        );

        EmployeeNationalIdentity result = service.upsert(
                new UpsertEmployeeNationalIdentityCommand(
                        employeeId,
                        "vn",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        " 001204012345 "
                )
        );

        assertEquals("VN", result.getCountryCode());
        assertEquals("2345", result.getLastFour());
        assertEquals("v1:iv:ciphertext", result.getEncryptedNumber());
        verify(identityRepository).save(any(EmployeeNationalIdentity.class));
    }

    @Test
    void rejectsNationalIdentityAssignedToAnotherEmployeeWithoutLeakingRawNumber() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository identityRepository = mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort = mock(NationalIdentityProtectionPort.class);
        EmployeeId employeeId = EmployeeId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV002", "Operator Two")
        ));
        when(protectionPort.fingerprint("001204012345")).thenReturn("fingerprint");
        when(identityRepository.existsByFingerprintAndEmployeeIdNot(
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "fingerprint",
                employeeId
        )).thenReturn(true);

        EmployeeNationalIdentityCommandService service = new EmployeeNationalIdentityCommandService(
                employeeRepository,
                identityRepository,
                protectionPort
        );

        NationalIdentityAlreadyAssignedException exception = assertThrows(
                NationalIdentityAlreadyAssignedException.class,
                () -> service.upsert(new UpsertEmployeeNationalIdentityCommand(
                        employeeId,
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "001204012345"
                ))
        );

        org.junit.jupiter.api.Assertions.assertFalse(exception.getMessage().contains("001204012345"));
    }

    @Test
    void rejectsIdentityForMissingEmployeeBeforeProtection() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository identityRepository = mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort = mock(NationalIdentityProtectionPort.class);
        EmployeeId employeeId = EmployeeId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        EmployeeNationalIdentityCommandService service = new EmployeeNationalIdentityCommandService(
                employeeRepository,
                identityRepository,
                protectionPort
        );

        assertThrows(EmployeeNotFoundException.class, () ->
                service.upsert(new UpsertEmployeeNationalIdentityCommand(
                        employeeId,
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "001204012345"
                ))
        );

        verifyNoInteractions(identityRepository, protectionPort);
    }
}