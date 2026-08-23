package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationQueryService {

    private final ApplicationRepository applicationRepository;

    public Application getById(ApplicationId id) {
        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(id)
                );
    }

    public Application getByCode(String code) {
        String normalizedCode = code == null
                ? null
                : code.trim().toUpperCase(java.util.Locale.ROOT);

        return applicationRepository.findByCode(normalizedCode)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(code)
                );
    }

    public Set<Application> getAllApplicationByStatus(ApplicationStatus  status) {
        return applicationRepository.findAllByStatus(status);
    }
}