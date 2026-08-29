package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ServicePrincipalRoleJpaId implements Serializable {

    @Column(name = "service_principal_id", nullable = false)
    private UUID servicePrincipalId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;
}