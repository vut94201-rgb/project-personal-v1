package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.directory;

import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountDirectoryProvisioningJpaRepository
        extends BaseJpaRepository<AccountDirectoryProvisioningJpaEntity, UUID> {

    Optional<AccountDirectoryProvisioningJpaEntity> findByAccountIdAndProvider(
            UUID accountId,
            DirectoryProviderType provider
    );
}