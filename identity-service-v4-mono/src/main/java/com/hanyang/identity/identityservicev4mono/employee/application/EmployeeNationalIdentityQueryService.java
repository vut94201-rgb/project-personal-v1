package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNationalIdentityNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import com.hanyang.identity.identityservicev4mono.employee.application.querry.EmployeeNationalIdentitySummary;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class EmployeeNationalIdentityQueryService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeNationalIdentityRepository nationalIdentityRepository;
    private final NationalIdentityProtectionPort protectionPort;

    public EmployeeNationalIdentitySummary getByEmployeeIdAndType(
            EmployeeId employeeId,
            String countryCode,
            NationalIdentityType identityType
    ) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        String normalizedCountryCode = NationalIdentityNumber.normalizeCountryCode(countryCode);
        EmployeeNationalIdentity identity = nationalIdentityRepository.findByEmployeeIdAndType(
                        employeeId,
                        normalizedCountryCode,
                        identityType
                )
                .orElseThrow(() -> new EmployeeNationalIdentityNotFoundException(
                        employeeId,
                        normalizedCountryCode,
                        identityType
                ));

        return toSummary(identity);
    }

    public Optional<EmployeeId> findEmployeeIdByNumber(
            String countryCode,
            NationalIdentityType identityType,
            String number
    ) {
        String normalizedCountryCode = NationalIdentityNumber.normalizeCountryCode(countryCode);
        NationalIdentityNumber normalizedNumber = NationalIdentityNumber.of(
                normalizedCountryCode,
                identityType,
                number
        );
        String fingerprint = protectionPort.fingerprint(normalizedNumber.value());

        return nationalIdentityRepository.findByFingerprint(
                        normalizedCountryCode,
                        identityType,
                        fingerprint
                )
                .map(EmployeeNationalIdentity::getEmployeeId);
    }

    private static EmployeeNationalIdentitySummary toSummary(EmployeeNationalIdentity identity) {
        return new EmployeeNationalIdentitySummary(
                identity.getId(),
                identity.getEmployeeId(),
                identity.getCountryCode(),
                identity.getIdentityType(),
                identity.getMaskedNumber()
        );
    }
}