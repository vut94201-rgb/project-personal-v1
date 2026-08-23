package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJpaRepository jpaRepository;
    private final RolePersistenceMapper mapper;

    @Override
    public Set<Role> findAllByRoleStatus(@Nullable RoleStatus roleStatus) {
        return Set.of();
    }

    @Override
    public Role save(Role role) {
        return jpaRepository.findById(role.getId().value())
                .map(existing -> {
                    mapper.updateEntity(role, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> {
                    RoleJpaEntity entity = mapper.toEntity(role);

                    return mapper.toDomain(
                            jpaRepository.save(entity)
                    );
                });
    }

    @Override
    public Optional<Role> findById(RoleId id) {
        return jpaRepository.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Role> findAllByApplicationId(
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
    public List<Role> findAllByIds(List<RoleId> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        return jpaRepository
                .findAllById(
                        ids.stream()
                                .map(RoleId::value)
                                .toList()
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