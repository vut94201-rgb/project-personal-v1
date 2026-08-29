package com.hanyang.identity.identityservicev4mono.service_identity.application;

import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.AssignServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.CreateServicePrincipalCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.UpdateServicePrincipalCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class ServicePrincipalCommandService {

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalOwnershipCommandService ownershipCommandService;
    private final ServicePrincipalProvisioningService provisioningService;

    /**
     * Issues a machine identity as a business resource. A PRIMARY human owner
     * is mandatory before external provisioning is requested.
     */
    @Transactional
    public ServicePrincipal create(CreateServicePrincipalCommand command) {
        ServicePrincipal servicePrincipal = ServicePrincipal.create(
                ServicePrincipalId.newId(),
                command.code(),
                command.displayName(),
                command.purpose(),
                command.description()
        );

        if (servicePrincipalRepository.existsByCode(servicePrincipal.getCode())) {
            throw new ServicePrincipalCodeAlreadyExistsException(
                    servicePrincipal.getCode()
            );
        }

        ServicePrincipal saved = servicePrincipalRepository.save(servicePrincipal);

        ownershipCommandService.assign(
                new AssignServicePrincipalOwnerCommand(
                        saved.getId(),
                        command.primaryOwnerEmployeeId(),
                        ServicePrincipalOwnershipType.PRIMARY
                )
        );

        provisioningService.requestSynchronization(saved.getId());
        return saved;
    }

    @Transactional
    public ServicePrincipal update(UpdateServicePrincipalCommand command) {
        ServicePrincipal servicePrincipal = servicePrincipalRepository
                .findById(command.servicePrincipalId())
                .orElseThrow(() ->
                        new ServicePrincipalNotFoundException(
                                command.servicePrincipalId()
                        )
                );

        servicePrincipal.updateDetails(
                command.displayName(),
                command.purpose(),
                command.description()
        );

        ServicePrincipal saved = servicePrincipalRepository.save(servicePrincipal);
        provisioningService.requestSynchronization(saved.getId());
        return saved;
    }
}