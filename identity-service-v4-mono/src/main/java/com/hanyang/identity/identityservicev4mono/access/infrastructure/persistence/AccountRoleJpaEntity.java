package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_roles")
@Getter
@Setter
@NoArgsConstructor
public class AccountRoleJpaEntity extends AuditableEntity {

    @EmbeddedId
    private AccountRoleJpaId id;
}