package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServicePrincipalProvisioningJpaRepository
        extends JpaRepository<ServicePrincipalProvisioningJpaEntity, UUID> {

    Optional<ServicePrincipalProvisioningJpaEntity> findByServicePrincipalIdAndProvider(
            UUID servicePrincipalId,
            IdentityProviderType provider
    );
}