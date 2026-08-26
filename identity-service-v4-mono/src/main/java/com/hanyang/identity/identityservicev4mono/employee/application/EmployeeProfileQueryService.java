package com.hanyang.identity.identityservicev4mono.employee.application;


import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeProfileNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfile;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfileRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@IdentityReadAccess
public class EmployeeProfileQueryService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeProfileRepository profileRepository;

    public EmployeeProfile getByEmployeeId(EmployeeId employeeId) {
        employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        return profileRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new EmployeeProfileNotFoundException(employeeId));
    }
}