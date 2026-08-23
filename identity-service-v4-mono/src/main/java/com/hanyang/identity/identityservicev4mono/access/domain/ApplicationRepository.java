package com.hanyang.identity.identityservicev4mono.access.domain;


import java.util.Optional;
import java.util.Set;

public interface ApplicationRepository {

    Application save(Application application);

    Optional<Application> findById(ApplicationId id);

    Optional<Application> findByCode(String code);

    boolean existsByCode(String code);


    Set<Application> findAllByStatus(ApplicationStatus status);
}