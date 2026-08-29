package com.hanyang.identity.identityservicev4mono.access.application;


import com.hanyang.identity.identityservicev4mono.access.application.exception.*;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class ServicePrincipalRoleCommandService {

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final ServicePrincipalRoleRepository servicePrincipalRoleRepository;
    private final ServicePrincipalRoleProvisioningService provisioningService;

    /**
     * Assigns desired application access to a service principal.
     *
     * PENDING service principals are intentionally allowed here. Machine
     * identity access can be prepared before external provisioning completes;
     * the machine-identity provisioning flow materializes the desired
     * assignment after an external service-account client exists. DISABLED
     * principals cannot receive new access.
     */
    @Transactional
    public void assign(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        ServicePrincipal servicePrincipal = getServicePrincipal(servicePrincipalId);
        Role role = getRole(roleId);
        validateAssignable(servicePrincipal, role);

        if (servicePrincipalRoleRepository.exists(servicePrincipalId, roleId)) {
            throw new ServicePrincipalRoleAlreadyAssignedException(
                    servicePrincipalId,
                    roleId
            );
        }

        servicePrincipalRoleRepository.save(
                ServicePrincipalRole.create(servicePrincipalId, roleId)
        );

        // PENDING principals intentionally keep desired access local until the
        // machine identity has an external provider representation. The
        // service-principal provisioning flow will emit all assigned roles
        // after the Keycloak client/service-account exists.
        if (servicePrincipal.getStatus() == ServicePrincipalStatus.ACTIVE) {
            provisioningService.requestSynchronization(
                    servicePrincipalId,
                    roleId,
                    true
            );
        }
    }

    @Transactional
    public void revoke(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        ServicePrincipal servicePrincipal = getServicePrincipal(servicePrincipalId);
        getRole(roleId);

        if (!servicePrincipalRoleRepository.exists(servicePrincipalId, roleId)) {
            throw new ServicePrincipalRoleNotAssignedException(
                    servicePrincipalId,
                    roleId
            );
        }

        servicePrincipalRoleRepository.delete(servicePrincipalId, roleId);

        // A PENDING principal has not been materialized for machine access yet,
        // so there is nothing external to revoke. ACTIVE
        // or DISABLED principals may already carry a provider-side mapping and
        // must converge to desiredAssigned=false.
        if (servicePrincipal.getStatus() != ServicePrincipalStatus.PENDING) {
            provisioningService.requestSynchronization(
                    servicePrincipalId,
                    roleId,
                    false
            );
        }
    }

    private ServicePrincipal getServicePrincipal(ServicePrincipalId servicePrincipalId) {
        return servicePrincipalRepository
                .findById(servicePrincipalId)
                .orElseThrow(() -> new ServicePrincipalNotFoundException(servicePrincipalId));
    }

    private Role getRole(RoleId roleId) {
        return roleRepository
                .findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    private void validateAssignable(
            ServicePrincipal servicePrincipal,
            Role role
    ) {
        if (servicePrincipal.getStatus() == ServicePrincipalStatus.DISABLED) {
            throw new ServicePrincipalDisabledException(servicePrincipal.getId());
        }

        if (role.getStatus() != RoleStatus.ACTIVE) {
            throw new RoleDisabledException(role.getId());
        }

        Application application = applicationRepository
                .findById(role.getApplicationId())
                .orElseThrow(() ->
                        new ApplicationNotFoundException(role.getApplicationId())
                );

        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationDisabledException(application.getId());
        }
    }
}