package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.organization.application.exception.CrewNotFoundException;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@IdentityReadAccess
@Transactional(readOnly = true)
public class CrewQueryService {
    private final CrewRepository crewRepository;

    public Crew getById(CrewId id) {
        return crewRepository.findById(id)
                .orElseThrow(() -> new CrewNotFoundException(id));
    }

    public List<Crew> findByDepartment(
            DepartmentId departmentId,
            @Nullable OrganizationReferenceStatus status
    ) {
        return crewRepository.findAllByDepartmentIdAndStatus(departmentId, status);
    }
}