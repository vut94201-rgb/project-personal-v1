package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence.converter.OrganizationReferenceStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PositionJpaEntity extends AuditableEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Convert(converter = OrganizationReferenceStatusConverter.class)
    @Column(name = "status", nullable = false, length = 3)
    private OrganizationReferenceStatus status;
}
