package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ApplicationJpaRepository
        extends BaseJpaRepository<ApplicationJpaEntity, UUID> {

    Optional<ApplicationJpaEntity> findByCode(String code);

    boolean existsByCode(String code);

  @Query(
"""
        select a from ApplicationJpaEntity  a where (:status is null  or  a.status=:status)
        """)
  Set<ApplicationJpaEntity> findAllByApplicationStatus(@Param("status") ApplicationStatus status);
}