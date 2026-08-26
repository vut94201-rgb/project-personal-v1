package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.directory;


import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.AccountJpaEntity;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "account_directory_bindings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_account_directory_bindings_account_provider",
                columnNames = {"account_id", "provider"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class AccountDirectoryProvisioningJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_account_directory_bindings_account")
    )
    private AccountJpaEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private DirectoryProviderType provider;

    @Column(name = "external_dn", length = 500)
    private String externalDn;

    @Column(name = "external_code", length = 100)
    private String externalCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private AccountDirectoryProvisioningStatus syncStatus;

    @Column(name = "desired_revision", nullable = false)
    private long desiredRevision;

    @Column(name = "synced_revision", nullable = false)
    private long syncedRevision;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_error", length = 2000)
    private String lastError;
}