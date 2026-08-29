package com.hanyang.identity.identityservicev4mono.service_identity.application;

import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.AssignServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.RevokeServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalOwnerAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalOwnerEmployeeNotActiveException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalPrimaryOwnerAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServicePrincipalOwnershipCommandServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-28T09:30:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void assignsPrimaryOwnerForActiveEmployee() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ServicePrincipalId servicePrincipalId = ServicePrincipalId.newId();
        EmployeeId employeeId = EmployeeId.newId();

        when(principalRepository.findById(servicePrincipalId)).thenReturn(Optional.of(
                ServicePrincipal.create(
                        servicePrincipalId,
                        "MES_SYNC",
                        "MES Sync",
                        "Synchronize MES data",
                        null
                )
        ));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV001", "Owner One")
        ));
        when(ownerRepository.existsActiveByServicePrincipalIdAndEmployeeId(
                servicePrincipalId, employeeId
        )).thenReturn(false);
        when(ownerRepository.existsActivePrimaryByServicePrincipalId(servicePrincipalId))
                .thenReturn(false);
        when(ownerRepository.save(any(ServicePrincipalOwner.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        ServicePrincipalOwner result = service.assign(
                new AssignServicePrincipalOwnerCommand(
                        servicePrincipalId,
                        employeeId,
                        ServicePrincipalOwnershipType.PRIMARY
                )
        );

        assertEquals(servicePrincipalId, result.getServicePrincipalId());
        assertEquals(employeeId, result.getEmployeeId());
        assertEquals(ServicePrincipalOwnershipType.PRIMARY, result.getOwnershipType());
        assertEquals(ServicePrincipalOwnerStatus.ACTIVE, result.getStatus());
        verify(ownerRepository).save(any(ServicePrincipalOwner.class));
    }

    @Test
    void rejectsInactiveEmployee() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ServicePrincipalId servicePrincipalId = ServicePrincipalId.newId();
        EmployeeId employeeId = EmployeeId.newId();

        when(principalRepository.findById(servicePrincipalId)).thenReturn(Optional.of(
                ServicePrincipal.create(
                        servicePrincipalId,
                        "REPORTER",
                        "Reporter",
                        "Produce reports",
                        null
                )
        ));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.rehydrate(
                        employeeId,
                        "NV002",
                        "Inactive Owner",
                        EmployeeStatus.INACTIVE
                )
        ));

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        assertThrows(ServicePrincipalOwnerEmployeeNotActiveException.class, () ->
                service.assign(new AssignServicePrincipalOwnerCommand(
                        servicePrincipalId,
                        employeeId,
                        ServicePrincipalOwnershipType.TECHNICAL
                ))
        );

        verify(ownerRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateActiveOwner() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ServicePrincipalId servicePrincipalId = ServicePrincipalId.newId();
        EmployeeId employeeId = EmployeeId.newId();

        when(principalRepository.findById(servicePrincipalId)).thenReturn(Optional.of(
                ServicePrincipal.create(
                        servicePrincipalId,
                        "AUTOMATION",
                        "Automation",
                        "Automation workload",
                        null
                )
        ));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV003", "Owner Three")
        ));
        when(ownerRepository.existsActiveByServicePrincipalIdAndEmployeeId(
                servicePrincipalId, employeeId
        )).thenReturn(true);

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        assertThrows(ServicePrincipalOwnerAlreadyAssignedException.class, () ->
                service.assign(new AssignServicePrincipalOwnerCommand(
                        servicePrincipalId,
                        employeeId,
                        ServicePrincipalOwnershipType.TECHNICAL
                ))
        );
    }

    @Test
    void rejectsSecondActivePrimaryOwner() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ServicePrincipalId servicePrincipalId = ServicePrincipalId.newId();
        EmployeeId employeeId = EmployeeId.newId();

        when(principalRepository.findById(servicePrincipalId)).thenReturn(Optional.of(
                ServicePrincipal.create(
                        servicePrincipalId,
                        "SYNC_AGENT",
                        "Sync Agent",
                        "Synchronization workload",
                        null
                )
        ));
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV004", "Owner Four")
        ));
        when(ownerRepository.existsActiveByServicePrincipalIdAndEmployeeId(
                servicePrincipalId, employeeId
        )).thenReturn(false);
        when(ownerRepository.existsActivePrimaryByServicePrincipalId(servicePrincipalId))
                .thenReturn(true);

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        assertThrows(ServicePrincipalPrimaryOwnerAlreadyExistsException.class, () ->
                service.assign(new AssignServicePrincipalOwnerCommand(
                        servicePrincipalId,
                        employeeId,
                        ServicePrincipalOwnershipType.PRIMARY
                ))
        );
    }

    @Test
    void revokesOwnerUsingInjectedClock() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ServicePrincipalOwnerId ownerId = ServicePrincipalOwnerId.newId();
        ServicePrincipalOwner owner = ServicePrincipalOwner.create(
                ownerId,
                ServicePrincipalId.newId(),
                EmployeeId.newId(),
                ServicePrincipalOwnershipType.TECHNICAL
        );

        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(ownerRepository.save(owner)).thenReturn(owner);

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        ServicePrincipalOwner result = service.revoke(
                new RevokeServicePrincipalOwnerCommand(ownerId)
        );

        assertEquals(ServicePrincipalOwnerStatus.REVOKED, result.getStatus());
        assertEquals(NOW, result.getRevokedAt());
        verify(ownerRepository).save(owner);
    }

    @Test
    void rejectsDirectPrimaryOwnerRevocation() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        ServicePrincipalOwnerId ownerId = ServicePrincipalOwnerId.newId();
        ServicePrincipalOwner owner = ServicePrincipalOwner.create(
                ownerId,
                ServicePrincipalId.newId(),
                EmployeeId.newId(),
                ServicePrincipalOwnershipType.PRIMARY
        );

        when(ownerRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        assertThrows(
                com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalPrimaryOwnerRevocationNotAllowedException.class,
                () -> service.revoke(new RevokeServicePrincipalOwnerCommand(ownerId))
        );

        verify(ownerRepository, never()).save(any());
    }

    @Test
    void transfersPrimaryOwnerAtomicallyAtApplicationLayer() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnerRepository ownerRepository = mock(ServicePrincipalOwnerRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);

        ServicePrincipalId servicePrincipalId = ServicePrincipalId.newId();
        EmployeeId oldEmployeeId = EmployeeId.newId();
        EmployeeId newEmployeeId = EmployeeId.newId();

        ServicePrincipal principal = ServicePrincipal.create(
                servicePrincipalId,
                "TRANSFER_TEST",
                "Transfer Test",
                "Test ownership transfer",
                null
        );
        ServicePrincipalOwner currentPrimary = ServicePrincipalOwner.create(
                ServicePrincipalOwnerId.newId(),
                servicePrincipalId,
                oldEmployeeId,
                ServicePrincipalOwnershipType.PRIMARY
        );

        when(principalRepository.findById(servicePrincipalId))
                .thenReturn(Optional.of(principal));
        when(employeeRepository.findById(newEmployeeId))
                .thenReturn(Optional.of(
                        Employee.create(newEmployeeId, "NV999", "New Owner")
                ));
        when(ownerRepository.findAllActiveByServicePrincipalId(servicePrincipalId))
                .thenReturn(java.util.List.of(currentPrimary));
        when(ownerRepository.save(any(ServicePrincipalOwner.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServicePrincipalOwnershipCommandService service = new ServicePrincipalOwnershipCommandService(
                principalRepository,
                ownerRepository,
                employeeRepository,
                CLOCK
        );

        ServicePrincipalOwner replacement = service.transferPrimaryOwner(
                servicePrincipalId,
                newEmployeeId
        );

        assertEquals(ServicePrincipalOwnerStatus.REVOKED, currentPrimary.getStatus());
        assertEquals(NOW, currentPrimary.getRevokedAt());
        assertEquals(newEmployeeId, replacement.getEmployeeId());
        assertEquals(ServicePrincipalOwnershipType.PRIMARY, replacement.getOwnershipType());
        assertEquals(ServicePrincipalOwnerStatus.ACTIVE, replacement.getStatus());
    }

}