package com.hanyang.identity.identityservicev4mono.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;
import java.util.Objects;

@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 100)
    private String deletedBy;

    public void softDelete(String actor, Instant deletedAt) {
        if (this.deleted) {
            return;
        }

        this.deleted = true;
        this.deletedAt =
                Objects.requireNonNull(deletedAt, "deletedAt must not be null");
        this.deletedBy =
                Objects.requireNonNull(actor, "actor must not be null");
    }

    public void restore() {
        if (!this.deleted) {
            return;
        }

        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }
}