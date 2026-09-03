package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.NationalIdentityAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.employee.application.port.EmployeeCodeGenerator;
import com.hanyang.identity.identityservicev4mono.employee.application.port.NationalIdentityProtectionPort;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityNumber;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmployeeOnboardingPreparationService {

    static final String V1_COUNTRY_CODE = "VN";
    static final NationalIdentityType V1_IDENTITY_TYPE = NationalIdentityType.NATIONAL_ID_CARD;

    private final EmployeeNationalIdentityRepository nationalIdentityRepository;
    private final NationalIdentityProtectionPort protectionPort;
    private final EmployeeCodeGenerator employeeCodeGenerator;

    public EmployeeOnboardingPreparation prepare(StartEmployeeOnboardingCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        NationalIdentityNumber number = NationalIdentityNumber.of(
                V1_COUNTRY_CODE,
                V1_IDENTITY_TYPE,
                command.nationalIdentityNumber()
        );

        String fingerprint = protectionPort.fingerprint(number.value());

        if (nationalIdentityRepository.findByFingerprint(
                V1_COUNTRY_CODE,
                V1_IDENTITY_TYPE,
                fingerprint
        ).isPresent()) {
            throw new NationalIdentityAlreadyAssignedException(
                    V1_COUNTRY_CODE,
                    V1_IDENTITY_TYPE
            );
        }

        String encryptedNumber = protectionPort.encrypt(number.value());

        return new EmployeeOnboardingPreparation(
                EmployeeId.newId(),
                employeeCodeGenerator.nextCode(),
                V1_COUNTRY_CODE,
                V1_IDENTITY_TYPE,
                encryptedNumber,
                fingerprint,
                number.lastFour()
        );
    }
}