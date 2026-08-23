package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.RoleProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.RoleJpaEntity;
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
        name = "role_identity_provider_bindings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_role_idp_bindings_role_provider",
                columnNames = {"role_id", "provider"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class RoleProvisioningJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "role_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_role_idp_bindings_role")
    )
    private RoleJpaEntity role;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "provider",
            nullable = false,
            length = 30
    )
    private IdentityProviderType provider;

    @Column(
            name = "external_id",
            length = 100
    )
    private String externalId;

    @Column(
            name = "external_code",
            length = 100
    )
    private String externalCode;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "sync_status",
            nullable = false,
            length = 20
    )
    private RoleProvisioningStatus syncStatus;

    @Column(
            name = "desired_revision",
            nullable = false
    )
    private long desiredRevision;

    @Column(
            name = "synced_revision",
            nullable = false
    )
    private long syncedRevision;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(
            name = "last_error",
            length = 2000
    )
    private String lastError;
}