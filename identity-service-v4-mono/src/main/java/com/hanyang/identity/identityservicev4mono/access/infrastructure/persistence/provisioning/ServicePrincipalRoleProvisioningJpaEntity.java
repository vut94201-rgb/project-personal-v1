package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "service_principal_role_identity_provider_bindings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_service_principal_role_idp_bindings_key_provider",
                columnNames = {
                        "service_principal_id",
                        "role_id",
                        "provider"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ServicePrincipalRoleProvisioningJpaEntity
        extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "service_principal_id", nullable = false)
    private UUID servicePrincipalId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private IdentityProviderType provider;

    @Column(name = "desired_assigned", nullable = false)
    private boolean desiredAssigned;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private ServicePrincipalRoleProvisioningStatus syncStatus;

    @Column(name = "desired_revision", nullable = false)
    private long desiredRevision;

    @Column(name = "synced_revision", nullable = false)
    private long syncedRevision;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;
}