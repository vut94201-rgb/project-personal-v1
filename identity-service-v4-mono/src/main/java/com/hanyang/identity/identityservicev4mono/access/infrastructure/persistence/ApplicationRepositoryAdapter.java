package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ApplicationRepositoryAdapter implements ApplicationRepository {

  private final ApplicationJpaRepository jpaRepository;
  private final ApplicationPersistenceMapper mapper;

  @Override
  public Application save(Application application) {
    return jpaRepository
        .findById(application.getId().value())
        .map(
            existing -> {
              mapper.updateEntity(application, existing);
              return mapper.toDomain(existing);
            })
        .orElseGet(
            () -> {
              ApplicationJpaEntity entity = mapper.toEntity(application);

              return mapper.toDomain(jpaRepository.save(entity));
            });
  }

  @Override
  public Optional<Application> findById(ApplicationId id) {
    return jpaRepository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public Optional<Application> findByCode(String code) {
    return jpaRepository.findByCode(code).map(mapper::toDomain);
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }

  @Override
  public Set<Application> findAllByStatus(ApplicationStatus status) {
    return jpaRepository.findAllByApplicationStatus(status).stream()
        .parallel()
        .map(mapper::toDomain)
        .collect(Collectors.toSet());
  }
}
