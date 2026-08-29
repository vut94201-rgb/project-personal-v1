package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.provisioning;

import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.ServicePrincipalJpaEntity;
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
        name = "service_principal_identity_provider_bindings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_service_principal_idp_bindings_principal_provider",
                columnNames = {"service_principal_id", "provider"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ServicePrincipalProvisioningJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "service_principal_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_service_principal_idp_bindings_principal")
    )
    private ServicePrincipalJpaEntity servicePrincipal;

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
    private ServicePrincipalProvisioningStatus syncStatus;

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