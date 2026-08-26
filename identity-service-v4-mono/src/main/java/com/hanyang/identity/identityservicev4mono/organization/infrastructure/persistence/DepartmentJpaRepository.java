package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentJpaRepository extends BaseJpaRepository<DepartmentJpaEntity, UUID> {
    Optional<DepartmentJpaEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("select d from DepartmentJpaEntity d where (:status is null or d.status = :status) order by d.code")
    List<DepartmentJpaEntity> findAllByDepartmentStatus(@Param("status") OrganizationReferenceStatus status);
}
