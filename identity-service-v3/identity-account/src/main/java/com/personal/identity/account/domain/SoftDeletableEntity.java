package com.personal.identity.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class SoftDeletableEntity extends AuditableEntity {

  @Column(name = "deleted", nullable = false)
  private boolean deleted;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @Column(name = "deleted_by", length = 100)
  private String deletedBy;

  public void softDelete(String actor, Instant deletedAt) {
    if (this.deleted) return;
    this.deleted = true;
    this.deletedAt = deletedAt;
    this.deletedBy = actor;
  }

  public void restore() {
    this.deletedBy = null;
    this.deletedAt = null;
    this.deleted = false;
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
