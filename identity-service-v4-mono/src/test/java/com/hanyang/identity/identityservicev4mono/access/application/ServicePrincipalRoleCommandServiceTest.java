package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.exception.ServicePrincipalDisabledException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ServicePrincipalRoleAlreadyAssignedException;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ServicePrincipalRoleCommandServiceTest {

    @Test
    void pendingServicePrincipalCanReceiveDesiredRoleAssignment() {
        ServicePrincipal principal = pendingServicePrincipal();
        Application application = activeApplication();
        Role role = activeRole(application);

        ServicePrincipalRepository servicePrincipalRepository = mock(ServicePrincipalRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ServicePrincipalRoleRepository assignmentRepository = mock(ServicePrincipalRoleRepository.class);
        ServicePrincipalRoleProvisioningService provisioningService =
                mock(ServicePrincipalRoleProvisioningService.class);

        when(servicePrincipalRepository.findById(principal.getId()))
                .thenReturn(Optional.of(principal));
        when(roleRepository.findById(role.getId()))
                .thenReturn(Optional.of(role));
        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(assignmentRepository.exists(principal.getId(), role.getId()))
                .thenReturn(false);

        ServicePrincipalRoleCommandService service = new ServicePrincipalRoleCommandService(
                servicePrincipalRepository,
                roleRepository,
                applicationRepository,
                assignmentRepository,
                provisioningService
        );

        service.assign(principal.getId(), role.getId());

        verify(assignmentRepository).save(any(ServicePrincipalRole.class));
        verifyNoInteractions(provisioningService);
    }

    @Test
    void disabledServicePrincipalCannotReceiveNewRole() {
        ServicePrincipal principal = pendingServicePrincipal();
        principal.disable();
        Application application = activeApplication();
        Role role = activeRole(application);

        ServicePrincipalRepository servicePrincipalRepository = mock(ServicePrincipalRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ServicePrincipalRoleRepository assignmentRepository = mock(ServicePrincipalRoleRepository.class);
        ServicePrincipalRoleProvisioningService provisioningService =
                mock(ServicePrincipalRoleProvisioningService.class);

        when(servicePrincipalRepository.findById(principal.getId()))
                .thenReturn(Optional.of(principal));
        when(roleRepository.findById(role.getId()))
                .thenReturn(Optional.of(role));

        ServicePrincipalRoleCommandService service = new ServicePrincipalRoleCommandService(
                servicePrincipalRepository,
                roleRepository,
                applicationRepository,
                assignmentRepository,
                provisioningService
        );

        assertThrows(
                ServicePrincipalDisabledException.class,
                () -> service.assign(principal.getId(), role.getId())
        );

        verify(assignmentRepository, never()).save(any(ServicePrincipalRole.class));
    }

    @Test
    void duplicateRoleAssignmentIsRejected() {
        ServicePrincipal principal = pendingServicePrincipal();
        Application application = activeApplication();
        Role role = activeRole(application);

        ServicePrincipalRepository servicePrincipalRepository = mock(ServicePrincipalRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        ServicePrincipalRoleRepository assignmentRepository = mock(ServicePrincipalRoleRepository.class);
        ServicePrincipalRoleProvisioningService provisioningService =
                mock(ServicePrincipalRoleProvisioningService.class);

        when(servicePrincipalRepository.findById(principal.getId()))
                .thenReturn(Optional.of(principal));
        when(roleRepository.findById(role.getId()))
                .thenReturn(Optional.of(role));
        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(assignmentRepository.exists(principal.getId(), role.getId()))
                .thenReturn(true);

        ServicePrincipalRoleCommandService service = new ServicePrincipalRoleCommandService(
                servicePrincipalRepository,
                roleRepository,
                applicationRepository,
                assignmentRepository,
                provisioningService
        );

        assertThrows(
                ServicePrincipalRoleAlreadyAssignedException.class,
                () -> service.assign(principal.getId(), role.getId())
        );

        verify(assignmentRepository, never()).save(any(ServicePrincipalRole.class));
    }


    @Test
    void activeServicePrincipalRoleAssignmentRequestsProviderSynchronization() {
        ServicePrincipal principal = pendingServicePrincipal();
        principal.activate();
        Application application = activeApplication();
        Role role = activeRole(application);

        ServicePrincipalRepository servicePrincipalRepository =
                mock(ServicePrincipalRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ApplicationRepository applicationRepository =
                mock(ApplicationRepository.class);
        ServicePrincipalRoleRepository assignmentRepository =
                mock(ServicePrincipalRoleRepository.class);
        ServicePrincipalRoleProvisioningService provisioningService =
                mock(ServicePrincipalRoleProvisioningService.class);

        when(servicePrincipalRepository.findById(principal.getId()))
                .thenReturn(Optional.of(principal));
        when(roleRepository.findById(role.getId()))
                .thenReturn(Optional.of(role));
        when(applicationRepository.findById(application.getId()))
                .thenReturn(Optional.of(application));
        when(assignmentRepository.exists(principal.getId(), role.getId()))
                .thenReturn(false);

        ServicePrincipalRoleCommandService service =
                new ServicePrincipalRoleCommandService(
                        servicePrincipalRepository,
                        roleRepository,
                        applicationRepository,
                        assignmentRepository,
                        provisioningService
                );

        service.assign(principal.getId(), role.getId());

        verify(provisioningService).requestSynchronization(
                principal.getId(),
                role.getId(),
                true
        );
    }

    private ServicePrincipal pendingServicePrincipal() {
        return ServicePrincipal.create(
                ServicePrincipalId.newId(),
                "MES_SYNC",
                "MES Sync",
                "Synchronize manufacturing data",
                null
        );
    }

    private Application activeApplication() {
        return Application.create(
                ApplicationId.newId(),
                "OQC",
                "OQC"
        );
    }

    private Role activeRole(Application application) {
        return Role.create(
                RoleId.newId(),
                application.getId(),
                "OQC_LOT_IMPORTER",
                "OQC Lot Importer"
        );
    }
}