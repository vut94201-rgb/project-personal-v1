package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Permission;
import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import com.hanyang.identity.identityservicev4mono.access.domain.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PermissionRepositoryAdapter implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionPersistenceMapper mapper;

    @Override
    public Permission save(Permission permission) {
        return jpaRepository.findById(permission.getId().value())
                .map(existing -> {
                    mapper.updateEntity(permission, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> {
                    PermissionJpaEntity entity = mapper.toEntity(permission);

                    return mapper.toDomain(
                            jpaRepository.save(entity)
                    );
                });
    }

    @Override
    public Optional<Permission> findById(PermissionId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Permission> findAllByIds(
            Collection<PermissionId> ids
    ) {
        return jpaRepository
                .findAllById(
                        ids.stream()
                                .map(PermissionId::value)
                                .toList()
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Permission> findAllByApplicationId(
            ApplicationId applicationId
    ) {
        return jpaRepository
                .findAllByApplicationIdOrderByCodeAsc(
                        applicationId.value()
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByApplicationIdAndCode(
            ApplicationId applicationId,
            String code
    ) {
        return jpaRepository.existsByApplicationIdAndCode(
                applicationId.value(),
                code
        );
    }
}