package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.organization.application.exception.DepartmentNotFoundException;
import com.hanyang.identity.identityservicev4mono.organization.domain.Department;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentRepository;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@IdentityReadAccess
@Transactional(readOnly = true)
public class DepartmentQueryService {
    private final DepartmentRepository departmentRepository;

    public Department getById(DepartmentId id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException(id));
    }

    public Department getByCode(String code) {
        String normalized = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        return departmentRepository.findByCode(normalized)
                .orElseThrow(() -> new DepartmentNotFoundException(code));
    }

    public List<Department> findAll(@Nullable OrganizationReferenceStatus status) {
        return departmentRepository.findAllByStatus(status);
    }
}
