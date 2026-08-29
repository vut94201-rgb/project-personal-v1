package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ServicePrincipalRoleProvisioningServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-28T00:00:00Z");

    @Test
    void assignedRolesAreEmittedThroughOutbox() {
        ServicePrincipalId principalId = ServicePrincipalId.newId();
        RoleId first = RoleId.newId();
        RoleId second = RoleId.newId();

        ServicePrincipalRoleRepository assignmentRepository =
                mock(ServicePrincipalRoleRepository.class);
        ServicePrincipalRoleProvisioningStateRepository stateRepository =
                mock(ServicePrincipalRoleProvisioningStateRepository.class);
        OutboxPublisher outboxPublisher = mock(OutboxPublisher.class);

        when(assignmentRepository.findRoleIdsByServicePrincipalId(principalId))
                .thenReturn(List.of(first, second));

        ServicePrincipalRoleProvisioningService service = service(
                mock(ServicePrincipalRepository.class),
                mock(RoleRepository.class),
                mock(ApplicationRepository.class),
                assignmentRepository,
                stateRepository,
                mock(ServicePrincipalProvisioningStateRepository.class),
                mock(RoleProvisioningService.class),
                mock(RoleProvisioningStateRepository.class),
                mock(IdentityProviderServicePrincipalAccessPort.class),
                outboxPublisher
        );

        service.requestAssignedRolesSynchronization(principalId);

        verify(stateRepository).requestSynchronization(
                principalId,
                first,
                IdentityProviderType.KEYCLOAK,
                true
        );
        verify(stateRepository).requestSynchronization(
                principalId,
                second,
                IdentityProviderType.KEYCLOAK,
                true
        );
        verify(outboxPublisher, times(2)).publish(
                eq(ServicePrincipalRoleProvisioningService.OUTBOX_AGGREGATE_TYPE),
                anyString(),
                eq(ServicePrincipalRoleProvisioningService.OUTBOX_EVENT_TYPE),
                isNull()
        );
    }

    @Test
    void reconcileAssignsApplicationClientRoleToMachineIdentity() {
        ServicePrincipalId principalId = ServicePrincipalId.newId();
        Application application = Application.create(
                ApplicationId.newId(),
                "OQC",
                "OQC"
        );
        Role role = Role.create(
                RoleId.newId(),
                application.getId(),
                "OQC_LOT_IMPORTER",
                "OQC Lot Importer"
        );
        ServicePrincipal principal = ServicePrincipal.create(
                principalId,
                "MES_SYNC",
                "MES Sync",
                "Synchronize MES data",
                null
        );
        principal.activate();

        ServicePrincipalRepository principalRepository =
                mock(ServicePrincipalRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);
        ServicePrincipalRoleRepository assignmentRepository =
                mock(ServicePrincipalRoleRepository.class);
        ServicePrincipalRoleProvisioningStateRepository stateRepository =
                mock(ServicePrincipalRoleProvisioningStateRepository.class);
        ServicePrincipalProvisioningStateRepository principalStateRepository =
                mock(ServicePrincipalProvisioningStateRepository.class);
        RoleProvisioningStateRepository roleStateRepository =
                mock(RoleProvisioningStateRepository.class);
        IdentityProviderServicePrincipalAccessPort providerPort =
                mock(IdentityProviderServicePrincipalAccessPort.class);

        ServicePrincipalRoleProvisioningState syncing =
                ServicePrincipalRoleProvisioningState.pending(
                        principalId,
                        role.getId(),
                        IdentityProviderType.KEYCLOAK,
                        true
                );
        syncing.beginSynchronization();

        ServicePrincipalRoleProvisioningState synced =
                ServicePrincipalRoleProvisioningState.rehydrate(
                        syncing.getId(),
                        principalId,
                        role.getId(),
                        IdentityProviderType.KEYCLOAK,
                        true,
                        ServicePrincipalRoleProvisioningStatus.SYNCED,
                        1,
                        1,
                        NOW,
                        null
                );

        ServicePrincipalProvisioningState principalBinding =
                ServicePrincipalProvisioningState.rehydrate(
                        UUID.randomUUID(),
                        principalId,
                        IdentityProviderType.KEYCLOAK,
                        "kc-service-client-1",
                        "svc-mes-sync",
                        ServicePrincipalProvisioningStatus.SYNCED,
                        2,
                        2,
                        NOW,
                        null
                );

        RoleProvisioningState roleBinding =
                RoleProvisioningState.rehydrate(
                        UUID.randomUUID(),
                        role.getId(),
                        IdentityProviderType.KEYCLOAK,
                        "kc-role-1",
                        role.getCode(),
                        RoleProvisioningStatus.SYNCED,
                        1,
                        1,
                        NOW,
                        null
                );

        when(stateRepository.findByKeyAndProvider(
                principalId,
                role.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(syncing));
        when(stateRepository.beginSynchronization(
                principalId,
                role.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(syncing);
        when(principalRepository.findById(principalId))
                .thenReturn(Optional.of(principal));
        when(roleRepository.findById(role.getId()))
                .thenReturn(Optional.of(role));
        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(principalStateRepository.findByServicePrincipalIdAndProvider(
                principalId,
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(principalBinding));
        when(roleStateRepository.findByRoleIdAndProvider(
                role.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(roleBinding));
        when(stateRepository.completeSynchronization(
                principalId,
                role.getId(),
                IdentityProviderType.KEYCLOAK,
                1,
                NOW
        )).thenReturn(synced);

        ServicePrincipalRoleProvisioningService service = service(
                principalRepository,
                roleRepository,
                applicationRepository,
                assignmentRepository,
                stateRepository,
                principalStateRepository,
                mock(RoleProvisioningService.class),
                roleStateRepository,
                providerPort,
                mock(OutboxPublisher.class)
        );

        ServicePrincipalRoleReconciliationResult result =
                service.reconcile(principalId, role.getId());

        assertEquals(
                ServicePrincipalRoleProvisioningStatus.SYNCED,
                result.status()
        );
        verify(providerPort).assignRole(
                "kc-service-client-1",
                "OQC",
                "OQC_LOT_IMPORTER"
        );
    }

    private static ServicePrincipalRoleProvisioningService service(
            ServicePrincipalRepository principalRepository,
            RoleRepository roleRepository,
            ApplicationRepository applicationRepository,
            ServicePrincipalRoleRepository assignmentRepository,
            ServicePrincipalRoleProvisioningStateRepository stateRepository,
            ServicePrincipalProvisioningStateRepository principalStateRepository,
            RoleProvisioningService roleProvisioningService,
            RoleProvisioningStateRepository roleStateRepository,
            IdentityProviderServicePrincipalAccessPort providerPort,
            OutboxPublisher outboxPublisher
    ) {
        return new ServicePrincipalRoleProvisioningService(
                principalRepository,
                roleRepository,
                applicationRepository,
                assignmentRepository,
                stateRepository,
                principalStateRepository,
                roleProvisioningService,
                roleStateRepository,
                providerPort,
                outboxPublisher,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }
}