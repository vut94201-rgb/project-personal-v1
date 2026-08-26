package com.hanyang.identity.identityservicev4mono.organization.domain;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;

public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findById(PositionId id);
    Optional<Position> findByCode(String code);
    boolean existsByCode(String code);
    List<Position> findAllByStatus(@Nullable OrganizationReferenceStatus status);
}
