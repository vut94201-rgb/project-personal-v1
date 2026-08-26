package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionJpaRepository extends BaseJpaRepository<PositionJpaEntity, UUID> {
    Optional<PositionJpaEntity> findByCode(String code);
    boolean existsByCode(String code);

    @Query("select p from PositionJpaEntity p where (:status is null or p.status = :status) order by p.code")
    List<PositionJpaEntity> findAllByPositionStatus(@Param("status") OrganizationReferenceStatus status);
}
