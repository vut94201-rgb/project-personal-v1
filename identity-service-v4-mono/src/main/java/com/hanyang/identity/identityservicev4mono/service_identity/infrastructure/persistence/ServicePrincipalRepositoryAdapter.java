package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ServicePrincipalRepositoryAdapter
        implements ServicePrincipalRepository {

    private final ServicePrincipalJpaRepository jpaRepository;
    private final ServicePrincipalPersistenceMapper mapper;

    @Override
    public ServicePrincipal save(ServicePrincipal servicePrincipal) {
        return jpaRepository
                .findById(servicePrincipal.getId().value())
                .map(existing -> {
                    mapper.updateEntity(servicePrincipal, existing);
                    return mapper.toDomain(existing);
                })
                .orElseGet(() -> {
                    ServicePrincipalJpaEntity entity =
                            mapper.toEntity(servicePrincipal);
                    return mapper.toDomain(jpaRepository.save(entity));
                });
    }

    @Override
    public Optional<ServicePrincipal> findById(ServicePrincipalId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<ServicePrincipal> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<ServicePrincipal> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Set<ServicePrincipal> findAllByStatus(
            ServicePrincipalStatus status
    ) {
        return jpaRepository.findAllByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toSet());
    }
}