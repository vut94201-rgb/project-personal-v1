package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountProvisioningJpaRepository
        extends JpaRepository<AccountProvisioningJpaEntity, UUID> {

    Optional<AccountProvisioningJpaEntity> findByAccountIdAndProvider(
            UUID accountId,
            IdentityProviderType provider
    );
}