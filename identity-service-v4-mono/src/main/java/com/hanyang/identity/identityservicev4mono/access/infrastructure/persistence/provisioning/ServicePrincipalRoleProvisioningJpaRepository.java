package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServicePrincipalRoleProvisioningJpaRepository
        extends BaseJpaRepository<ServicePrincipalRoleProvisioningJpaEntity, UUID> {

    Optional<ServicePrincipalRoleProvisioningJpaEntity>
    findByServicePrincipalIdAndRoleIdAndProvider(
            UUID servicePrincipalId,
            UUID roleId,
            IdentityProviderType provider
    );
}