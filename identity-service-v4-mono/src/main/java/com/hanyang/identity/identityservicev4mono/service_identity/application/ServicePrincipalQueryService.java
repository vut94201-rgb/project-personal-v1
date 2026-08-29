package com.hanyang.identity.identityservicev4mono.service_identity.application;


import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.*;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@IdentityReadAccess
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePrincipalQueryService {

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalOwnerRepository ownerRepository;
    private final ServicePrincipalProvisioningStateRepository provisioningStateRepository;

    public ServicePrincipalView getById(ServicePrincipalId id) {
        return toView(requireById(id));
    }

    public ServicePrincipalView getByCode(String code) {
        String normalized = code == null
                ? null
                : code.trim().toUpperCase(Locale.ROOT);

        ServicePrincipal servicePrincipal = servicePrincipalRepository
                .findByCode(normalized)
                .orElseThrow(() ->
                        new ServicePrincipalNotFoundException(code)
                );

        return toView(servicePrincipal);
    }

    public List<ServicePrincipalView> list(ServicePrincipalStatus status) {
        return (status == null
                ? servicePrincipalRepository.findAll().stream()
                : servicePrincipalRepository.findAllByStatus(status).stream())
                .sorted(Comparator.comparing(ServicePrincipal::getCode))
                .map(this::toView)
                .toList();
    }

    private ServicePrincipal requireById(ServicePrincipalId id) {
        return servicePrincipalRepository.findById(id)
                .orElseThrow(() -> new ServicePrincipalNotFoundException(id));
    }

    private ServicePrincipalView toView(ServicePrincipal servicePrincipal) {
        return new ServicePrincipalView(
                servicePrincipal,
                provisioningStateRepository
                        .findByServicePrincipalIdAndProvider(
                                servicePrincipal.getId(),
                                PROVIDER
                        )
                        .orElse(null),
                ownerRepository.findAllActiveByServicePrincipalId(
                        servicePrincipal.getId()
                )
        );
    }
}