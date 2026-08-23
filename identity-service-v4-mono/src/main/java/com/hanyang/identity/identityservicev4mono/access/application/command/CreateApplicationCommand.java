package com.hanyang.identity.identityservicev4mono.access.application.command;

public record CreateApplicationCommand(
        String code,
        String name
) {
}