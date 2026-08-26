package com.hanyang.identity.identityservicev4mono.organization.application.command;

import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;

public record UpdateCrewCommand(CrewId crewId, String name) {
}