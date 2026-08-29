package com.hanyang.identity.identityservicev4mono.service_identity.application.lifecycle;

import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicePrincipalActivationCoordinatorTest {

    @Test
    void currentProviderBindingActivatesPendingPrincipalAndSchedulesEnablement() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = ServicePrincipal.create(
                id,
                "MES_SYNC_AGENT",
                "MES Sync Agent",
                "Synchronize MES data",
                null
        );

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningStateRepository stateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(principalRepository.findById(id))
                .thenReturn(Optional.of(principal));
        when(stateRepository.findByServicePrincipalIdAndProvider(
                id,
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(synced(id, "kc-client-1")));

        ServicePrincipalActivationCoordinator coordinator =
                new ServicePrincipalActivationCoordinator(
                        principalRepository,
                        stateRepository,
                        outboxPublisher
                );

        coordinator.afterIdentityProviderSynchronization(id);

        assertEquals(ServicePrincipalStatus.ACTIVE, principal.getStatus());
        verify(principalRepository).save(principal);
        verify(stateRepository).requestSynchronization(
                id,
                IdentityProviderType.KEYCLOAK
        );
        verify(outboxPublisher).publish(
                ServicePrincipalProvisioningService.OUTBOX_AGGREGATE_TYPE,
                id.value().toString(),
                ServicePrincipalProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
    }

    @Test
    void syncedStateWithoutExternalBindingIsRejected() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = ServicePrincipal.create(
                id,
                "MES_SYNC_AGENT",
                "MES Sync Agent",
                "Synchronize MES data",
                null
        );

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningStateRepository stateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(principalRepository.findById(id))
                .thenReturn(Optional.of(principal));
        when(stateRepository.findByServicePrincipalIdAndProvider(
                id,
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(synced(id, null)));

        ServicePrincipalActivationCoordinator coordinator =
                new ServicePrincipalActivationCoordinator(
                        principalRepository,
                        stateRepository,
                        outboxPublisher
                );

        assertThrows(
                IllegalStateException.class,
                () -> coordinator.afterIdentityProviderSynchronization(id)
        );

        assertEquals(ServicePrincipalStatus.PENDING, principal.getStatus());
        verify(principalRepository, never()).save(any());
        verify(outboxPublisher, never())
                .publish(anyString(), anyString(), anyString(), any());
    }

    private static ServicePrincipalProvisioningState synced(
            ServicePrincipalId id,
            String externalId
    ) {
        return ServicePrincipalProvisioningState.rehydrate(
                UUID.randomUUID(),
                id,
                IdentityProviderType.KEYCLOAK,
                externalId,
                externalId == null ? null : "svc-mes-sync-agent",
                ServicePrincipalProvisioningStatus.SYNCED,
                1,
                1,
                Instant.parse("2026-08-28T15:00:00Z"),
                null
        );
    }
}