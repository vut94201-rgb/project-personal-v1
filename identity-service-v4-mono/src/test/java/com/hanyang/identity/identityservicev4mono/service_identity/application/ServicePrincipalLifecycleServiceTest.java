package com.hanyang.identity.identityservicev4mono.service_identity.application;

import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ServicePrincipalLifecycleServiceTest {

    @Test
    void disableChangesBusinessStateBeforeRequestingProviderConvergence() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = ServicePrincipal.create(
                id,
                "MES_SYNC_AGENT",
                "MES Sync Agent",
                "Synchronize MES data",
                null
        );
        principal.activate();

        ServicePrincipalRepository repository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningService provisioningService =
                mock(ServicePrincipalProvisioningService.class);

        when(repository.findById(id))
                .thenReturn(Optional.of(principal));

        ServicePrincipalLifecycleService service =
                new ServicePrincipalLifecycleService(
                        repository,
                        provisioningService
                );

        service.disable(id);

        assertEquals(ServicePrincipalStatus.DISABLED, principal.getStatus());
        verify(repository).save(principal);
        verify(provisioningService).requestSynchronization(id);
    }

    @Test
    void repeatedDisableIsIdempotentAndDoesNotCreateAnotherRevision() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = ServicePrincipal.create(
                id,
                "MES_SYNC_AGENT",
                "MES Sync Agent",
                "Synchronize MES data",
                null
        );
        principal.disable();

        ServicePrincipalRepository repository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningService provisioningService =
                mock(ServicePrincipalProvisioningService.class);

        when(repository.findById(id))
                .thenReturn(Optional.of(principal));

        ServicePrincipalLifecycleService service =
                new ServicePrincipalLifecycleService(
                        repository,
                        provisioningService
                );

        service.disable(id);

        verify(repository, never()).save(any());
        verifyNoInteractions(provisioningService);
    }
}