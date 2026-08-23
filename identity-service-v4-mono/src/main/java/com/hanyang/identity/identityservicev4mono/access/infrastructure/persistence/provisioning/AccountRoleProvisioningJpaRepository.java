package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRoleProvisioningJpaRepository
     extends JpaRepository<AccountRoleProvisioningJpaEntity, UUID> {

                Optional<AccountRoleProvisioningJpaEntity> findByAccountIdAndRoleIdAndProvider(
                        UUID accountId,
                 UUID roleId,
                        IdentityProviderType provider
                        );
    }