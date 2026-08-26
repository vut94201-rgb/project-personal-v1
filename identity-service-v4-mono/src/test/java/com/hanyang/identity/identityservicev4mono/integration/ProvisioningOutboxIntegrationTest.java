package com.hanyang.identity.identityservicev4mono.integration;


import com.hanyang.identity.identityservicev4mono.access.application.AccountRoleCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.ApplicationCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.RoleCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.command.CreateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.application.command.CreateRoleCommand;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.*;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import com.hanyang.identity.identityservicev4mono.account.application.AccountCommandService;
import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeCommandService;
import com.hanyang.identity.identityservicev4mono.employee.application.command.CreateEmployeeCommand;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.shared.directory.DirectoryProviderType;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.operations.provisioning.ProvisioningHealthReport;
import com.hanyang.identity.identityservicev4mono.shared.operations.provisioning.ProvisioningHealthService;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventStatus;
import com.hanyang.identity.identityservicev4mono.shared.outbox.persistence.OutboxEventJpaEntity;
import com.hanyang.identity.identityservicev4mono.shared.outbox.persistence.OutboxEventJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:identity-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
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
        "operations.provisioning-health.provider-probe-enabled=false",
        "integration.keycloak.admin-client-secret=test-secret"
})
@ActiveProfiles("test")
@Transactional
class ProvisioningOutboxIntegrationTest {

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    @Autowired
    private ApplicationCommandService applicationCommandService;

    @Autowired
    private RoleCommandService roleCommandService;

    @Autowired
    private EmployeeCommandService employeeCommandService;

    @Autowired
    private AccountCommandService accountCommandService;

    @Autowired
    private AccountRoleCommandService accountRoleCommandService;

    @Autowired
    private ApplicationProvisioningStateRepository applicationProvisioningStateRepository;

    @Autowired
    private RoleProvisioningStateRepository roleProvisioningStateRepository;

    @Autowired
    private AccountDirectoryProvisioningStateRepository accountDirectoryProvisioningStateRepository;

    @Autowired
    private AccountRoleProvisioningStateRepository accountRoleProvisioningStateRepository;

    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ProvisioningHealthService provisioningHealthService;

    @Test
    void localCommandsCreateProvisioningStateAndOutboxWithoutCallingIdentityProvider() {
        Application application = applicationCommandService.create(
                new CreateApplicationCommand("OQC", "OQC Service")
        );
        Role role = roleCommandService.create(
                new CreateRoleCommand(application.getId(), "OQC_OPERATOR", "OQC Operator")
        );
        Employee employee = employeeCommandService.create(
                new CreateEmployeeCommand("EMP001", "Test Employee")
        );
        Account account = accountCommandService.create(
                new CreateAccountCommand(employee.getId(), "emp001")
        );

        accountRoleCommandService.assign(account.getId(), role.getId());
        accountRoleCommandService.revoke(account.getId(), role.getId());

        entityManager.flush();
        entityManager.clear();

        ApplicationProvisioningState applicationState = applicationProvisioningStateRepository
                .findByApplicationIdAndProvider(application.getId(), PROVIDER)
                .orElseThrow();
        RoleProvisioningState roleState = roleProvisioningStateRepository
                .findByRoleIdAndProvider(role.getId(), PROVIDER)
                .orElseThrow();
        AccountDirectoryProvisioningState accountDirectoryState =
                accountDirectoryProvisioningStateRepository
                        .findByAccountIdAndProvider(
                                account.getId(),
                                DirectoryProviderType.DS389
                        )
                        .orElseThrow();
        AccountRoleProvisioningState accountRoleState = accountRoleProvisioningStateRepository
                .findByKeyAndProvider(account.getId(), role.getId(), PROVIDER)
                .orElseThrow();

        assertEquals(ApplicationProvisioningStatus.PENDING, applicationState.getStatus());
        assertEquals(1, applicationState.getDesiredRevision());
        assertEquals(0, applicationState.getSyncedRevision());

        assertEquals(RoleProvisioningStatus.PENDING, roleState.getStatus());
        assertEquals(1, roleState.getDesiredRevision());
        assertEquals(0, roleState.getSyncedRevision());

        assertEquals(
                AccountDirectoryProvisioningStatus.PENDING,
                accountDirectoryState.getStatus()
        );
        assertEquals(1, accountDirectoryState.getDesiredRevision());
        assertEquals(0, accountDirectoryState.getSyncedRevision());

        assertEquals(AccountRoleProvisioningStatus.PENDING, accountRoleState.getStatus());
        assertFalse(accountRoleState.isDesiredAssigned());
        assertEquals(2, accountRoleState.getDesiredRevision());
        assertEquals(0, accountRoleState.getSyncedRevision());

        String applicationAggregateId = application.getId().value().toString();
        String roleAggregateId = role.getId().value().toString();
        String accountAggregateId = account.getId().value().toString();
        String accountRoleAggregateId = account.getId().value() + ":" + role.getId().value();

        List<OutboxEventJpaEntity> events = outboxEventJpaRepository.findAll().stream()
                .filter(event -> isExpectedAggregate(
                        event,
                        applicationAggregateId,
                        roleAggregateId,
                        accountAggregateId,
                        accountRoleAggregateId
                ))
                .toList();

        assertEquals(5, events.size());
        assertTrue(events.stream().allMatch(event -> event.getStatus() == OutboxEventStatus.PENDING));
        assertTrue(events.stream().allMatch(event -> event.getAttemptCount() == 0));

        assertEquals(1, countEvent(events, "APPLICATION", applicationAggregateId));
        assertEquals(1, countEvent(events, "ROLE", roleAggregateId));
        assertEquals(1, countEvent(events, "ACCOUNT", accountAggregateId));
        assertTrue(events.stream()
                .filter(event -> "ACCOUNT".equals(event.getAggregateType()))
                .filter(event -> accountAggregateId.equals(event.getAggregateId()))
                .allMatch(event -> "ACCOUNT_DIRECTORY_PROVISIONING_REQUESTED"
                        .equals(event.getEventType())));
        assertEquals(2, countEvent(events, "ACCOUNT_ROLE", accountRoleAggregateId));
    }

    @Test
    void provisioningHealthReportsCurrentPendingBacklog() {
        applicationCommandService.create(
                new CreateApplicationCommand("WMS", "Warehouse Management")
        );

        entityManager.flush();

        ProvisioningHealthReport report = provisioningHealthService.inspect();
        ProvisioningHealthReport.ResourceHealth applicationHealth = report.resources().stream()
                .filter(resource -> "APPLICATION".equals(resource.resource()))
                .findFirst()
                .orElseThrow();

        assertEquals("HEALTHY", report.status());
        assertEquals("SKIPPED", report.provider().status());
        assertEquals(1, applicationHealth.pending());
        assertEquals(0, applicationHealth.failed());
        assertEquals(0, applicationHealth.drifted());
    }

    @Test
    void repeatedApplicationChangesAdvanceDesiredRevisionAndKeepEveryTriggerDurable() {
        Application application = applicationCommandService.create(
                new CreateApplicationCommand("MES", "MES Service")
        );

        applicationCommandService.update(
                new UpdateApplicationCommand(application.getId(), "MES Core")
        );
        applicationCommandService.update(
                new UpdateApplicationCommand(application.getId(), "MES Platform")
        );
        applicationCommandService.disable(application.getId());

        entityManager.flush();
        entityManager.clear();

        ApplicationProvisioningState state = applicationProvisioningStateRepository
                .findByApplicationIdAndProvider(application.getId(), PROVIDER)
                .orElseThrow();

        assertEquals(ApplicationProvisioningStatus.PENDING, state.getStatus());
        assertEquals(4, state.getDesiredRevision());
        assertEquals(0, state.getSyncedRevision());

        String applicationAggregateId = application.getId().value().toString();
        List<OutboxEventJpaEntity> applicationEvents = outboxEventJpaRepository.findAll().stream()
                .filter(event -> "APPLICATION".equals(event.getAggregateType()))
                .filter(event -> applicationAggregateId.equals(event.getAggregateId()))
                .filter(event -> "APPLICATION_PROVISIONING_REQUESTED".equals(event.getEventType()))
                .toList();

        assertEquals(4, applicationEvents.size());
    }

    private static boolean isExpectedAggregate(
            OutboxEventJpaEntity event,
            String applicationAggregateId,
            String roleAggregateId,
            String accountAggregateId,
            String accountRoleAggregateId
    ) {
        return switch (event.getAggregateType()) {
            case "APPLICATION" -> applicationAggregateId.equals(event.getAggregateId());
            case "ROLE" -> roleAggregateId.equals(event.getAggregateId());
            case "ACCOUNT" -> accountAggregateId.equals(event.getAggregateId());
            case "ACCOUNT_ROLE" -> accountRoleAggregateId.equals(event.getAggregateId());
            default -> false;
        };
    }

    private static long countEvent(
            List<OutboxEventJpaEntity> events,
            String aggregateType,
            String aggregateId
    ) {
        return events.stream()
                .filter(event -> aggregateType.equals(event.getAggregateType()))
                .filter(event -> aggregateId.equals(event.getAggregateId()))
                .count();
    }
}