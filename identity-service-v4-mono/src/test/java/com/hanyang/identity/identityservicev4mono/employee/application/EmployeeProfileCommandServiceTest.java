package com.hanyang.identity.identityservicev4mono.employee.application;


import com.hanyang.identity.identityservicev4mono.employee.application.command.UpdateEmployeeProfileCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeEmailAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmployeeProfileCommandServiceTest {

    @Test
    void createsProfileForExistingEmployee() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeProfileRepository profileRepository = mock(EmployeeProfileRepository.class);
        EmployeeId employeeId = EmployeeId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV001", "Operator One")
        ));
        when(profileRepository.findByEmployeeId(employeeId)).thenReturn(Optional.empty());
        when(profileRepository.existsByEmailIgnoreCaseAndEmployeeIdNot("user@hanyang.com", employeeId))
                .thenReturn(false);
        when(profileRepository.save(any(EmployeeProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EmployeeProfileCommandService service = new EmployeeProfileCommandService(
                employeeRepository,
                profileRepository
        );

        EmployeeProfile result = service.upsert(new UpdateEmployeeProfileCommand(
                employeeId,
                "USER@HANYANG.COM",
                "0901234567",
                "Bac Ninh",
                LocalDate.of(2026, 8, 25)
        ));

        assertEquals("user@hanyang.com", result.getEmail());
        verify(profileRepository).save(any(EmployeeProfile.class));
    }

    @Test
    void rejectsDuplicateEmailOwnedByAnotherEmployee() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeProfileRepository profileRepository = mock(EmployeeProfileRepository.class);
        EmployeeId employeeId = EmployeeId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV002", "Operator Two")
        ));
        when(profileRepository.existsByEmailIgnoreCaseAndEmployeeIdNot("same@hanyang.com", employeeId))
                .thenReturn(true);

        EmployeeProfileCommandService service = new EmployeeProfileCommandService(
                employeeRepository,
                profileRepository
        );

        assertThrows(EmployeeEmailAlreadyExistsException.class, () ->
                service.upsert(new UpdateEmployeeProfileCommand(
                        employeeId,
                        "same@hanyang.com",
                        null,
                        null,
                        null
                ))
        );
    }

    @Test
    void rejectsProfileForMissingEmployee() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeProfileRepository profileRepository = mock(EmployeeProfileRepository.class);
        EmployeeId employeeId = EmployeeId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        EmployeeProfileCommandService service = new EmployeeProfileCommandService(
                employeeRepository,
                profileRepository
        );

        assertThrows(EmployeeNotFoundException.class, () ->
                service.upsert(new UpdateEmployeeProfileCommand(
                        employeeId,
                        null,
                        null,
                        null,
                        null
                ))
        );

        verifyNoInteractions(profileRepository);
    }
}