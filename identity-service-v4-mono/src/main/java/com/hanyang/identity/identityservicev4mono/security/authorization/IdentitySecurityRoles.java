package com.hanyang.identity.identityservicev4mono.security.authorization;

public final class IdentitySecurityRoles {

    public static final String ADMIN = "IDENTITY_ADMIN";
    public static final String VIEWER = "IDENTITY_VIEWER";

    public static final String ADMIN_AUTHORITY = "ROLE_" + ADMIN;
    public static final String VIEWER_AUTHORITY = "ROLE_" + VIEWER;

    private IdentitySecurityRoles() {
    }
}