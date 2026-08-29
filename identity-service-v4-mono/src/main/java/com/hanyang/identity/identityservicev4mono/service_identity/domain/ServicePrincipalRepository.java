package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import java.util.List;
import java.util.Optional;
import java.util.Set;
public interface ServicePrincipalRepository {

    ServicePrincipal save(ServicePrincipal servicePrincipal);

    Optional<ServicePrincipal> findById(ServicePrincipalId id);

    Optional<ServicePrincipal> findByCode(String code);

    boolean existsByCode(String code);

    Set<ServicePrincipal> findAllByStatus(ServicePrincipalStatus status);

    List<ServicePrincipal> findAll();
}