package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServicePrincipalRoleQueryServiceTest {

    @Test
    void returnsAssignedRolesWithProvisioningState() {
        ServicePrincipalRepository principalRepository = mock(ServicePrincipalRepository.class);
        ServicePrincipalRoleRepository assignmentRepository =
                mock(ServicePrincipalRoleRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ServicePrincipalRoleProvisioningStateRepository provisioningRepository =
                mock(ServicePrincipalRoleProvisioningStateRepository.class);

        ServicePrincipalId principalId = ServicePrincipalId.newId();
        ApplicationId applicationId = ApplicationId.newId();
        RoleId roleId = RoleId.newId();
        Role role = Role.create(
                roleId,
                applicationId,
                "OQC_REPORT_READER",
                "OQC Report Reader"
        );

        when(principalRepository.findById(principalId)).thenReturn(Optional.of(
                ServicePrincipal.create(
                        principalId,
                        "REPORT_WORKER",
                        "Report Worker",
                        "Generate reports",
                        null
                )
        ));
        when(assignmentRepository.findRoleIdsByServicePrincipalId(principalId))
                .thenReturn(List.of(roleId));
        when(roleRepository.findAllByIds(List.of(roleId)))
                .thenReturn(List.of(role));

        ServicePrincipalRoleProvisioningState state =
                ServicePrincipalRoleProvisioningState.pending(
                        principalId,
                        roleId,
                        IdentityProviderType.KEYCLOAK,
                        true
                );
        when(provisioningRepository.findByKeyAndProvider(
                principalId,
                roleId,
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.of(state));

        ServicePrincipalRoleQueryService service =
                new ServicePrincipalRoleQueryService(
                        principalRepository,
                        assignmentRepository,
                        roleRepository,
                        provisioningRepository
                );

        List<ServicePrincipalRoleAssignmentView> result = service.list(principalId);

        assertEquals(1, result.size());
        assertEquals(roleId, result.getFirst().role().getId());
        assertEquals(state, result.getFirst().provisioning());
    }
}