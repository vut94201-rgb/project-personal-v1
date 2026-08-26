package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.organization.application.exception.PositionNotFoundException;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.organization.domain.Position;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionRepository;
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
public class PositionQueryService {
    private final PositionRepository positionRepository;

    public Position getById(PositionId id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new PositionNotFoundException(id));
    }

    public Position getByCode(String code) {
        String normalized = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        return positionRepository.findByCode(normalized)
                .orElseThrow(() -> new PositionNotFoundException(code));
    }

    public List<Position> findAll(@Nullable OrganizationReferenceStatus status) {
        return positionRepository.findAllByStatus(status);
    }
}
