package com.hanyang.identity.identityservicev4mono.employee.application;


import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EmployeeNationalIdentityQueryServiceTest {

    @Test
    void returnsMaskedSummaryWithoutRevealingEncryptedValue() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository identityRepository = mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort = mock(NationalIdentityProtectionPort.class);
        EmployeeId employeeId = EmployeeId.newId();

        EmployeeNationalIdentity identity = EmployeeNationalIdentity.create(
                EmployeeNationalIdentityId.newId(),
                employeeId,
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "v1:iv:ciphertext",
                "fingerprint",
                "2345"
        );

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV003", "Operator Three")
        ));
        when(identityRepository.findByEmployeeIdAndType(
                employeeId,
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD
        )).thenReturn(Optional.of(identity));

        EmployeeNationalIdentityQueryService service = new EmployeeNationalIdentityQueryService(
                employeeRepository,
                identityRepository,
                protectionPort
        );

        var result = service.getByEmployeeIdAndType(
                employeeId,
                "vn",
                NationalIdentityType.NATIONAL_ID_CARD
        );

        assertEquals("********2345", result.maskedNumber());
    }

    @Test
    void matchesEmployeeUsingFingerprintWithoutDecryptingStoredNumber() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository identityRepository = mock(EmployeeNationalIdentityRepository.class);
        NationalIdentityProtectionPort protectionPort = mock(NationalIdentityProtectionPort.class);
        EmployeeId employeeId = EmployeeId.newId();

        EmployeeNationalIdentity identity = EmployeeNationalIdentity.create(
                EmployeeNationalIdentityId.newId(),
                employeeId,
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "v1:iv:ciphertext",
                "fingerprint",
                "2345"
        );

        when(protectionPort.fingerprint("001204012345")).thenReturn("fingerprint");
        when(identityRepository.findByFingerprint(
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "fingerprint"
        )).thenReturn(Optional.of(identity));

        EmployeeNationalIdentityQueryService service = new EmployeeNationalIdentityQueryService(
                employeeRepository,
                identityRepository,
                protectionPort
        );

        Optional<EmployeeId> result = service.findEmployeeIdByNumber(
                "VN",
                NationalIdentityType.NATIONAL_ID_CARD,
                "001204012345"
        );

        assertEquals(Optional.of(employeeId), result);
    }
}