package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.organization.domain.Position;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PositionRepositoryAdapter implements PositionRepository {
    private final PositionJpaRepository jpaRepository;
    private final PositionPersistenceMapper mapper;

    @Override
    public Position save(Position position) {
        UUID id = position.getId().value();
        return jpaRepository.findById(id)
                .map(existing -> {
                    mapper.updateEntity(position, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> mapper.toDomain(jpaRepository.save(mapper.toEntity(position))));
    }

    @Override
    public Optional<Position> findById(PositionId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Position> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Position> findAllByStatus(@Nullable OrganizationReferenceStatus status) {
        return jpaRepository.findAllByPositionStatus(status).stream().map(mapper::toDomain).toList();
    }
}
