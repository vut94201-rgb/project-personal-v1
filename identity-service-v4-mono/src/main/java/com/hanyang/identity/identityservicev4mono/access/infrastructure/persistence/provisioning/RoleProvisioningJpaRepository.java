package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoleProvisioningJpaRepository
        extends BaseJpaRepository<RoleProvisioningJpaEntity, UUID> {

    @Query("""
            select binding
            from RoleProvisioningJpaEntity binding
            where binding.role.id = :roleId
              and binding.provider = :provider
            """)
    Optional<RoleProvisioningJpaEntity> findByRoleIdAndProvider(
            @Param("roleId") UUID roleId,
            @Param("provider") IdentityProviderType provider
    );
}