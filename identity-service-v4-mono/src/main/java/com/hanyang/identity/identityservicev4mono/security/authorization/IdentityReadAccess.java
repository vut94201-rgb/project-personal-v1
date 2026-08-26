package com.hanyang.identity.identityservicev4mono.security.authorization;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize(
        "hasAnyAuthority('" + IdentitySecurityRoles.ADMIN_AUTHORITY
                + "', '" + IdentitySecurityRoles.VIEWER_AUTHORITY + "')"
)
public @interface IdentityReadAccess {
}