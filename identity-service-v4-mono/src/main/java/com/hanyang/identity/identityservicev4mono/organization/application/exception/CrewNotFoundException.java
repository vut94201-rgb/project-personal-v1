package com.hanyang.identity.identityservicev4mono.organization.application.exception;


import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;

public class CrewNotFoundException extends RuntimeException {
    public CrewNotFoundException(CrewId crewId) {
        super("Crew not found: " + crewId.value());
    }
}