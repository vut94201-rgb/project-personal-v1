package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CrewRepositoryAdapter implements CrewRepository {
    private final CrewJpaRepository jpaRepository;
    private final CrewPersistenceMapper mapper;

    @Override
    public Crew save(Crew crew) {
        UUID id = crew.getId().value();
        return jpaRepository.findById(id)
                .map(existing -> {
                    mapper.updateEntity(crew, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> mapper.toDomain(jpaRepository.save(mapper.toEntity(crew))));
    }

    @Override
    public Optional<Crew> findById(CrewId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Crew> findByDepartmentIdAndCode(DepartmentId departmentId, String code) {
        return jpaRepository.findByDepartmentIdAndCode(departmentId.value(), code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByDepartmentIdAndCode(DepartmentId departmentId, String code) {
        return jpaRepository.existsByDepartmentIdAndCode(departmentId.value(), code);
    }

    @Override
    public List<Crew> findAllByDepartmentIdAndStatus(
            DepartmentId departmentId,
            @Nullable OrganizationReferenceStatus status
    ) {
        return jpaRepository.findAllByDepartmentAndStatus(departmentId.value(), status)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsActiveByDepartmentId(DepartmentId departmentId) {
        return jpaRepository.existsByDepartmentIdAndStatus(
                departmentId.value(),
                OrganizationReferenceStatus.ACTIVE
        );
    }
}