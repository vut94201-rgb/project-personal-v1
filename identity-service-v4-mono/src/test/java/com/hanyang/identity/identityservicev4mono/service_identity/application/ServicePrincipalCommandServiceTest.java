package com.hanyang.identity.identityservicev4mono.service_identity.application;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.AssignServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.CreateServicePrincipalCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.UpdateServicePrincipalCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicePrincipalCommandServiceTest {

    @Test
    void createAssignsPrimaryOwnerBeforeRequestingProvisioning() {
        ServicePrincipalRepository repository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnershipCommandService ownershipService =
                mock(ServicePrincipalOwnershipCommandService.class);
        ServicePrincipalProvisioningService provisioningService =
                mock(ServicePrincipalProvisioningService.class);
        EmployeeId ownerId = EmployeeId.newId();

        when(repository.existsByCode("MES_SYNC")).thenReturn(false);
        when(repository.save(any(ServicePrincipal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServicePrincipalCommandService service = new ServicePrincipalCommandService(
                repository,
                ownershipService,
                provisioningService
        );

        ServicePrincipal created = service.create(
                new CreateServicePrincipalCommand(
                        "mes_sync",
                        "MES Sync",
                        "Synchronize MES data",
                        "Machine-to-machine integration",
                        ownerId
                )
        );

        assertEquals("MES_SYNC", created.getCode());

        ArgumentCaptor<AssignServicePrincipalOwnerCommand> ownerCaptor =
                ArgumentCaptor.forClass(AssignServicePrincipalOwnerCommand.class);
        verify(ownershipService).assign(ownerCaptor.capture());
        assertEquals(created.getId(), ownerCaptor.getValue().servicePrincipalId());
        assertEquals(ownerId, ownerCaptor.getValue().employeeId());
        assertEquals(
                ServicePrincipalOwnershipType.PRIMARY,
                ownerCaptor.getValue().ownershipType()
        );

        verify(provisioningService).requestSynchronization(created.getId());
    }

    @Test
    void createRejectsDuplicateCodeBeforeOwnershipOrProvisioning() {
        ServicePrincipalRepository repository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnershipCommandService ownershipService =
                mock(ServicePrincipalOwnershipCommandService.class);
        ServicePrincipalProvisioningService provisioningService =
                mock(ServicePrincipalProvisioningService.class);

        when(repository.existsByCode("MES_SYNC")).thenReturn(true);

        ServicePrincipalCommandService service = new ServicePrincipalCommandService(
                repository,
                ownershipService,
                provisioningService
        );

        assertThrows(ServicePrincipalCodeAlreadyExistsException.class, () ->
                service.create(new CreateServicePrincipalCommand(
                        "MES_SYNC",
                        "MES Sync",
                        "Synchronize MES data",
                        null,
                        EmployeeId.newId()
                ))
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(ownershipService);
        verifyNoInteractions(provisioningService);
    }

    @Test
    void updateChangesDetailsAndRequestsProviderConvergence() {
        ServicePrincipalRepository repository = mock(ServicePrincipalRepository.class);
        ServicePrincipalOwnershipCommandService ownershipService =
                mock(ServicePrincipalOwnershipCommandService.class);
        ServicePrincipalProvisioningService provisioningService =
                mock(ServicePrincipalProvisioningService.class);
        ServicePrincipalId id = ServicePrincipalId.newId();

        ServicePrincipal existing = ServicePrincipal.create(
                id,
                "REPORT_WORKER",
                "Old name",
                "Old purpose",
                null
        );

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        ServicePrincipalCommandService service = new ServicePrincipalCommandService(
                repository,
                ownershipService,
                provisioningService
        );

        ServicePrincipal updated = service.update(
                new UpdateServicePrincipalCommand(
                        id,
                        "Reporting Worker",
                        "Generate scheduled OQC reports",
                        "Updated"
                )
        );

        assertEquals("Reporting Worker", updated.getDisplayName());
        assertEquals("Generate scheduled OQC reports", updated.getPurpose());
        assertEquals("Updated", updated.getDescription());
        verify(provisioningService).requestSynchronization(id);
    }
}