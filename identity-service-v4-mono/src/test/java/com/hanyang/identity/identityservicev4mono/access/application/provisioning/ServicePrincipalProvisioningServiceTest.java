package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.lifecycle.ServicePrincipalActivationCoordinator;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.IdentityProviderServicePrincipalPort;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.ServicePrincipalAccessSynchronizationPort;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.*;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicePrincipalProvisioningServiceTest {

    private static final Instant NOW =
            Instant.parse("2026-08-28T15:00:00Z");

    @Test
    void requestSynchronizationPersistsDesiredStateAndPublishesOutboxEvent() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = principal(id);

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningStateRepository stateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        IdentityProviderServicePrincipalPort providerPort =
                mock(IdentityProviderServicePrincipalPort.class);
        ServicePrincipalActivationCoordinator activationCoordinator =
                mock(ServicePrincipalActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(principalRepository.findById(id))
                .thenReturn(Optional.of(principal));

        ServicePrincipalProvisioningService service = service(
                principalRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher
        );

        service.requestSynchronization(id);

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
        verifyNoInteractions(providerPort);
    }

    @Test
    void missingPrincipalDoesNotCreateBindingOrOutboxEvent() {
        ServicePrincipalId id = ServicePrincipalId.newId();

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningStateRepository stateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        IdentityProviderServicePrincipalPort providerPort =
                mock(IdentityProviderServicePrincipalPort.class);
        ServicePrincipalActivationCoordinator activationCoordinator =
                mock(ServicePrincipalActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(principalRepository.findById(id))
                .thenReturn(Optional.empty());

        ServicePrincipalProvisioningService service = service(
                principalRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher
        );

        assertThrows(
                ServicePrincipalNotFoundException.class,
                () -> service.requestSynchronization(id)
        );

        verifyNoInteractions(
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher
        );
    }

    @Test
    void pendingPrincipalIsProvisionedDisabledThenCoordinatorRuns() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = principal(id);

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningStateRepository stateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        IdentityProviderServicePrincipalPort providerPort =
                mock(IdentityProviderServicePrincipalPort.class);
        ServicePrincipalActivationCoordinator activationCoordinator =
                mock(ServicePrincipalActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        ServicePrincipalProvisioningState syncing =
                ServicePrincipalProvisioningState.rehydrate(
                        UUID.randomUUID(),
                        id,
                        IdentityProviderType.KEYCLOAK,
                        null,
                        null,
                        ServicePrincipalProvisioningStatus.SYNCING,
                        1,
                        0,
                        null,
                        null
                );

        ServicePrincipalProvisioningState synced =
                ServicePrincipalProvisioningState.rehydrate(
                        syncing.getId(),
                        id,
                        IdentityProviderType.KEYCLOAK,
                        "kc-client-1",
                        "svc-mes-sync-agent",
                        ServicePrincipalProvisioningStatus.SYNCED,
                        1,
                        1,
                        NOW,
                        null
                );

        when(principalRepository.findById(id))
                .thenReturn(Optional.of(principal), Optional.of(principal));
        when(stateRepository.beginSynchronization(
                id,
                IdentityProviderType.KEYCLOAK
        )).thenReturn(syncing);
        when(providerPort.synchronizeServicePrincipal(
                "MES_SYNC_AGENT",
                "MES Sync Agent",
                "Synchronize MES data",
                null,
                false
        )).thenReturn(
                new IdentityProviderServicePrincipalPort.ProvisionedServicePrincipal(
                        "kc-client-1",
                        "svc-mes-sync-agent"
                )
        );
        when(stateRepository.completeSynchronization(
                id,
                IdentityProviderType.KEYCLOAK,
                1,
                "kc-client-1",
                "svc-mes-sync-agent",
                NOW
        )).thenReturn(synced);

        ServicePrincipalProvisioningService service = service(
                principalRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher
        );

        ServicePrincipalReconciliationResult result = service.reconcile(id);

        assertEquals(
                ServicePrincipalProvisioningStatus.SYNCED,
                result.status()
        );
        verify(activationCoordinator)
                .afterIdentityProviderSynchronization(id);
    }

    @Test
    void providerFailureIsPersistedAndDoesNotActivatePrincipal() {
        ServicePrincipalId id = ServicePrincipalId.newId();
        ServicePrincipal principal = principal(id);

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        ServicePrincipalProvisioningStateRepository stateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        IdentityProviderServicePrincipalPort providerPort =
                mock(IdentityProviderServicePrincipalPort.class);
        ServicePrincipalActivationCoordinator activationCoordinator =
                mock(ServicePrincipalActivationCoordinator.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        ServicePrincipalProvisioningState syncing =
                ServicePrincipalProvisioningState.rehydrate(
                        UUID.randomUUID(),
                        id,
                        IdentityProviderType.KEYCLOAK,
                        null,
                        null,
                        ServicePrincipalProvisioningStatus.SYNCING,
                        1,
                        0,
                        null,
                        null
                );

        ServicePrincipalProvisioningState failed =
                ServicePrincipalProvisioningState.rehydrate(
                        syncing.getId(),
                        id,
                        IdentityProviderType.KEYCLOAK,
                        null,
                        null,
                        ServicePrincipalProvisioningStatus.FAILED,
                        1,
                        0,
                        null,
                        "Keycloak unavailable"
                );

        when(principalRepository.findById(id))
                .thenReturn(Optional.of(principal));
        when(stateRepository.beginSynchronization(
                id,
                IdentityProviderType.KEYCLOAK
        )).thenReturn(syncing);
        when(providerPort.synchronizeServicePrincipal(
                anyString(),
                anyString(),
                anyString(),
                isNull(),
                eq(false)
        )).thenThrow(new IllegalStateException("Keycloak unavailable"));
        when(stateRepository.failSynchronization(
                id,
                IdentityProviderType.KEYCLOAK,
                1,
                "Keycloak unavailable"
        )).thenReturn(failed);

        ServicePrincipalProvisioningService service = service(
                principalRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                outboxPublisher
        );

        ServicePrincipalReconciliationResult result = service.reconcile(id);

        assertEquals(
                ServicePrincipalProvisioningStatus.FAILED,
                result.status()
        );
        verifyNoInteractions(activationCoordinator);
    }

    private static ServicePrincipalProvisioningService service(
            ServicePrincipalRepository principalRepository,
            ServicePrincipalProvisioningStateRepository stateRepository,
            IdentityProviderServicePrincipalPort providerPort,
            ServicePrincipalActivationCoordinator activationCoordinator,
            OutboxPublisher outboxPublisher
    ) {
        return new ServicePrincipalProvisioningService(
                principalRepository,
                stateRepository,
                providerPort,
                activationCoordinator,
                mock(ServicePrincipalAccessSynchronizationPort.class),
                outboxPublisher,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    private static ServicePrincipal principal(ServicePrincipalId id) {
        return ServicePrincipal.create(
                id,
                "MES_SYNC_AGENT",
                "MES Sync Agent",
                "Synchronize MES data",
                null
        );
    }
}