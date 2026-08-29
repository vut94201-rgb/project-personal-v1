package com.hanyang.identity.identityservicev4mono.service_identity.application;

import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business lifecycle commands for service principals.
 */
@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class ServicePrincipalLifecycleService {

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalProvisioningService provisioningService;

    /**
     * Disables the business principal first and publishes provider convergence
     * in the same local transaction. Remote Keycloak work remains asynchronous.
     */
    @Transactional
    public void disable(ServicePrincipalId servicePrincipalId) {
        ServicePrincipal servicePrincipal = servicePrincipalRepository
                .findById(servicePrincipalId)
                .orElseThrow(() ->
                        new ServicePrincipalNotFoundException(servicePrincipalId)
                );

        if (servicePrincipal.getStatus() == ServicePrincipalStatus.DISABLED) {
            return;
        }

        servicePrincipal.disable();
        servicePrincipalRepository.save(servicePrincipal);
        provisioningService.requestSynchronization(servicePrincipalId);
    }
}