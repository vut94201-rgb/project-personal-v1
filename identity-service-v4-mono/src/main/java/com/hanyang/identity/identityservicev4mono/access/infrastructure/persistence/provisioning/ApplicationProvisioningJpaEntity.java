package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.ApplicationJpaEntity;
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
        name = "application_identity_provider_bindings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_application_idp_bindings_application_provider",
                columnNames = {"application_id", "provider"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ApplicationProvisioningJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "application_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_application_idp_bindings_application")
    )
    private ApplicationJpaEntity application;

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
    private ApplicationProvisioningStatus syncStatus;

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