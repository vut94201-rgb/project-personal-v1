package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.converter.AccountStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "employee_id",
            nullable = false,
            unique = true
    )
    private UUID employeeId;

    @Column(
            name = "username",
            nullable = false,
            unique = true,
            length = 100
    )
    private String username;

    @Column(
            name = "keycloak_subject",
            unique = true,
            length = 100
    )
    private String keycloakSubject;

    @Convert(converter = AccountStatusConverter.class)
    @Column(
            name = "status",
            nullable = false,
            length = 3
    )
    private AccountStatus status;
}