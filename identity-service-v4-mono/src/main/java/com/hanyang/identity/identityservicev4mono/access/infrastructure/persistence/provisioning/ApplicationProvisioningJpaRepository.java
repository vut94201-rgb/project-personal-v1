package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ApplicationProvisioningJpaRepository
        extends BaseJpaRepository<ApplicationProvisioningJpaEntity, UUID> {

    @Query("""
            select binding
            from ApplicationProvisioningJpaEntity binding
            where binding.application.id = :applicationId
              and binding.provider = :provider
            """)
    Optional<ApplicationProvisioningJpaEntity> findByApplicationIdAndProvider(
            @Param("applicationId") UUID applicationId,
            @Param("provider") IdentityProviderType provider
    );
}