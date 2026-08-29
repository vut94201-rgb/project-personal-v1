package com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicePrincipalProvisioningOutboxHandlerTest {

    @Test
    void syncedReconciliationCompletesEventNormally() {
        ServicePrincipalProvisioningService service =
                mock(ServicePrincipalProvisioningService.class);
        ServicePrincipalId id = ServicePrincipalId.newId();

        when(service.reconcile(id)).thenReturn(
                new ServicePrincipalReconciliationResult(
                        id,
                        IdentityProviderType.KEYCLOAK,
                        ServicePrincipalProvisioningStatus.SYNCED,
                        "kc-client-1",
                        "svc-mes-sync-agent",
                        null
                )
        );

        ServicePrincipalProvisioningOutboxHandler handler =
                new ServicePrincipalProvisioningOutboxHandler(service);

        handler.handle(event(id));

        verify(service).reconcile(id);
    }

    @Test
    void failedReconciliationThrowsSoOutboxWorkerCanRetry() {
        ServicePrincipalProvisioningService service =
                mock(ServicePrincipalProvisioningService.class);
        ServicePrincipalId id = ServicePrincipalId.newId();

        when(service.reconcile(id)).thenReturn(
                ServicePrincipalReconciliationResult.failed(
                        id,
                        IdentityProviderType.KEYCLOAK,
                        "Keycloak unavailable"
                )
        );

        ServicePrincipalProvisioningOutboxHandler handler =
                new ServicePrincipalProvisioningOutboxHandler(service);

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(event(id))
        );
    }

    private static OutboxEvent event(ServicePrincipalId id) {
        return new OutboxEvent(
                UUID.randomUUID(),
                ServicePrincipalProvisioningService.OUTBOX_AGGREGATE_TYPE,
                id.value().toString(),
                ServicePrincipalProvisioningService.OUTBOX_EVENT_TYPE,
                null,
                1
        );
    }
}