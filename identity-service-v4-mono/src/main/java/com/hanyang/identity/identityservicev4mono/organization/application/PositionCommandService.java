package com.hanyang.identity.identityservicev4mono.organization.application;

import com.hanyang.identity.identityservicev4mono.organization.application.command.CreatePositionCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.command.UpdatePositionCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.PositionCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.PositionHasActiveAssignmentsException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.PositionNotFoundException;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentRepository;
import com.hanyang.identity.identityservicev4mono.organization.domain.Position;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class PositionCommandService {
    private final PositionRepository positionRepository;
    private final OrganizationalAssignmentRepository assignmentRepository;

    @Transactional
    public Position create(CreatePositionCommand command) {
        Position position = Position.create(PositionId.newId(), command.code(), command.name());
        if (positionRepository.existsByCode(position.getCode())) {
            throw new PositionCodeAlreadyExistsException(position.getCode());
        }
        return positionRepository.save(position);
    }

    @Transactional
    public Position update(UpdatePositionCommand command) {
        Position position = positionRepository.findById(command.positionId())
                .orElseThrow(() -> new PositionNotFoundException(command.positionId()));
        position.rename(command.name());
        return positionRepository.save(position);
    }

    @Transactional
    public void disable(PositionId positionId) {
        Position position = positionRepository.findById(positionId)
                .orElseThrow(() -> new PositionNotFoundException(positionId));
        if (assignmentRepository.existsActiveByPositionId(positionId)) {
            throw new PositionHasActiveAssignmentsException(positionId);
        }
        position.disable();
        positionRepository.save(position);
    }
}
