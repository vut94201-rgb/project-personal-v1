package com.hanyang.identity.identityservicev4mono.organization.application.command;

import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;

public record UpdatePositionCommand(PositionId positionId, String name) {
}
