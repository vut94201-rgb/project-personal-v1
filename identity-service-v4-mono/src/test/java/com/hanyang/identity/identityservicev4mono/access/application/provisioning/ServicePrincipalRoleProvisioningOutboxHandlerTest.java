package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ServicePrincipalRoleProvisioningOutboxHandlerTest {

    @Test
    void syncedResultCompletesEvent() {
        ServicePrincipalId principalId = ServicePrincipalId.newId();
        RoleId roleId = RoleId.newId();
        ServicePrincipalRoleProvisioningService service =
                mock(ServicePrincipalRoleProvisioningService.class);

        when(service.reconcile(principalId, roleId))
                .thenReturn(new ServicePrincipalRoleReconciliationResult(
                        principalId,
                        roleId,
                        IdentityProviderType.KEYCLOAK,
                        true,
                        ServicePrincipalRoleProvisioningStatus.SYNCED,
                        1,
                        1,
                        null
                ));

        ServicePrincipalRoleProvisioningOutboxHandler handler =
                new ServicePrincipalRoleProvisioningOutboxHandler(service);

        handler.handle(event(principalId, roleId));

        verify(service).reconcile(principalId, roleId);
    }

    @Test
    void failedResultIsRetriedByOutboxWorker() {
        ServicePrincipalId principalId = ServicePrincipalId.newId();
        RoleId roleId = RoleId.newId();
        ServicePrincipalRoleProvisioningService service =
                mock(ServicePrincipalRoleProvisioningService.class);

        when(service.reconcile(principalId, roleId))
                .thenReturn(new ServicePrincipalRoleReconciliationResult(
                        principalId,
                        roleId,
                        IdentityProviderType.KEYCLOAK,
                        true,
                        ServicePrincipalRoleProvisioningStatus.FAILED,
                        1,
                        0,
                        "Keycloak unavailable"
                ));

        ServicePrincipalRoleProvisioningOutboxHandler handler =
                new ServicePrincipalRoleProvisioningOutboxHandler(service);

        assertThrows(
                IllegalStateException.class,
                () -> handler.handle(event(principalId, roleId))
        );
    }

    private static OutboxEvent event(
            ServicePrincipalId principalId,
            RoleId roleId
    ) {
        return new OutboxEvent(
                UUID.randomUUID(),
                ServicePrincipalRoleProvisioningService.OUTBOX_AGGREGATE_TYPE,
                new ServicePrincipalRoleProvisioningKey(
                        principalId,
                        roleId
                ).serialize(),
                ServicePrincipalRoleProvisioningService.OUTBOX_EVENT_TYPE,
                null,
                1
        );
    }
}