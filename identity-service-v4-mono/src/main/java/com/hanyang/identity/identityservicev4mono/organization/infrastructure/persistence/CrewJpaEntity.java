package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence.converter.OrganizationReferenceStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "crews")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CrewJpaEntity extends AuditableEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Convert(converter = OrganizationReferenceStatusConverter.class)
    @Column(name = "status", nullable = false, length = 3)
    private OrganizationReferenceStatus status;
}