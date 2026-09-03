package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "employee_national_identities")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmployeeNationalIdentityJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", nullable = false, length = 32)
    private NationalIdentityType identityType;

    @Column(name = "encrypted_number", nullable = false, columnDefinition = "TEXT")
    private String encryptedNumber;

    @Column(name = "number_fingerprint", nullable = false, length = 64)
    private String numberFingerprint;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;
}