package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.application.command.UpsertEmployeeNationalIdentityCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.NationalIdentityAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class EmployeeNationalIdentityCommandService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeNationalIdentityRepository nationalIdentityRepository;
    private final NationalIdentityProtectionPort protectionPort;

    @Transactional
    public EmployeeNationalIdentity upsert(UpsertEmployeeNationalIdentityCommand command) {
        employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        String countryCode = NationalIdentityNumber.normalizeCountryCode(command.countryCode());
        NationalIdentityNumber number = NationalIdentityNumber.of(
                countryCode,
                command.identityType(),
                command.number()
        );

        String fingerprint = protectionPort.fingerprint(number.value());

        if (nationalIdentityRepository.existsByFingerprintAndEmployeeIdNot(
                countryCode,
                command.identityType(),
                fingerprint,
                command.employeeId()
        )) {
            throw new NationalIdentityAlreadyAssignedException(
                    countryCode,
                    command.identityType()
            );
        }

        String encryptedNumber = protectionPort.encrypt(number.value());

        EmployeeNationalIdentity identity = nationalIdentityRepository.findByEmployeeIdAndType(
                        command.employeeId(),
                        countryCode,
                        command.identityType()
                )
                .orElseGet(() -> EmployeeNationalIdentity.create(
                        EmployeeNationalIdentityId.newId(),
                        command.employeeId(),
                        countryCode,
                        command.identityType(),
                        encryptedNumber,
                        fingerprint,
                        number.lastFour()
                ));

        identity.updateProtectedNumber(
                encryptedNumber,
                fingerprint,
                number.lastFour()
        );

        return nationalIdentityRepository.save(identity);
    }
}