package com.hanyang.identity.identityservicev4mono.security;


import com.hanyang.identity.identityservicev4mono.access.application.ApplicationCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.ApplicationQueryService;
import com.hanyang.identity.identityservicev4mono.access.application.command.CreateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeCommandService;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeQueryService;
import com.hanyang.identity.identityservicev4mono.employee.application.command.CreateEmployeeCommand;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentitySecurityRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:identity-security-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.batch.jdbc.initialize-schema=never",
        "outbox.worker.enabled=false",
        "integration.keycloak.admin-client-secret=test-secret"
})
@ActiveProfiles("test")
@Import(IdentityMethodSecurityIntegrationTest.MethodSecurityTestConfiguration.class)
@Transactional
class IdentityMethodSecurityIntegrationTest {

    @Autowired
    private ApplicationCommandService applicationCommandService;

    @Autowired
    private ApplicationQueryService applicationQueryService;

    @Autowired
    private EmployeeCommandService employeeCommandService;

    @Autowired
    private EmployeeQueryService employeeQueryService;

    @Test
    @WithMockUser(authorities = IdentitySecurityRoles.VIEWER_AUTHORITY)
    void viewerCanReadButCannotMutate() {
        assertDoesNotThrow(() ->
                applicationQueryService.getAllApplicationByStatus(ApplicationStatus.ACTIVE)
        );

        assertThrows(
                AccessDeniedException.class,
                () -> applicationCommandService.create(
                        new CreateApplicationCommand("OQC", "OQC Service")
                )
        );
    }

    @Test
    @WithMockUser(authorities = IdentitySecurityRoles.ADMIN_AUTHORITY)
    void adminCanMutateAndRead() {
        Application application = applicationCommandService.create(
                new CreateApplicationCommand("MES", "MES Service")
        );

        Application loaded = applicationQueryService.getById(application.getId());

        assertEquals(application.getId(), loaded.getId());
        assertEquals("MES", loaded.getCode());
    }

    @Test
    @WithMockUser(authorities = "ROLE_OQC_OPERATOR")
    void unrelatedApplicationRoleCannotReadIdentityData() {
        assertThrows(
                AccessDeniedException.class,
                () -> applicationQueryService.getAllApplicationByStatus(
                        ApplicationStatus.ACTIVE
                )
        );
    }

    @Test
    @WithMockUser(authorities = IdentitySecurityRoles.VIEWER_AUTHORITY)
    void viewerCanReadEmployeesButCannotMutateThem() {
        assertDoesNotThrow(() -> employeeQueryService.findAllByEmployeeStatus(null));

        assertThrows(
                AccessDeniedException.class,
                () -> employeeCommandService.create(
                        new CreateEmployeeCommand("SEC-VIEWER", "Security Viewer")
                )
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EnableMethodSecurity
    static class MethodSecurityTestConfiguration {
    }
}