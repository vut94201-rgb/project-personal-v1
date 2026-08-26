package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.employee.application.command.UpdateEmployeeProfileCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeEmailAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfile;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfileRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class EmployeeProfileCommandService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeProfileRepository profileRepository;

    @Transactional
    public EmployeeProfile upsert(UpdateEmployeeProfileCommand command) {
        employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        if (StringUtils.hasText(command.email())
                && profileRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(
                command.email().trim(),
                command.employeeId()
        )) {
            throw new EmployeeEmailAlreadyExistsException(command.email().trim());
        }

        EmployeeProfile profile = profileRepository.findByEmployeeId(command.employeeId())
                .orElseGet(() -> EmployeeProfile.create(
                        command.employeeId(),
                        null,
                        null,
                        null,
                        null
                ));

        profile.update(
                command.email(),
                command.phoneNumber(),
                command.address(),
                command.hireDate()
        );

        return profileRepository.save(profile);
    }
}