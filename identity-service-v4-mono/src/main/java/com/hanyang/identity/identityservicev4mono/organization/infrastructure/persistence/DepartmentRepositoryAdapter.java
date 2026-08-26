package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.Department;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentRepository;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DepartmentRepositoryAdapter implements DepartmentRepository {
    private final DepartmentJpaRepository jpaRepository;
    private final DepartmentPersistenceMapper mapper;

    @Override
    public Department save(Department department) {
        UUID id = department.getId().value();
        return jpaRepository.findById(id)
                .map(existing -> {
                    mapper.updateEntity(department, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> mapper.toDomain(jpaRepository.save(mapper.toEntity(department))));
    }

    @Override
    public Optional<Department> findById(DepartmentId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Department> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Department> findAllByStatus(@Nullable OrganizationReferenceStatus status) {
        return jpaRepository.findAllByDepartmentStatus(status).stream().map(mapper::toDomain).toList();
    }
}
