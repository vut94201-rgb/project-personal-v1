package com.hanyang.identity.identityservicev4mono.account.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void linkingKeycloakSubjectDoesNotActivatePendingAccount() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );

        account.linkKeycloakSubject("kc-user-001");

        assertEquals("kc-user-001", account.getKeycloakSubject());
        assertEquals(AccountStatus.PENDING, account.getStatus());
    }

    @Test
    void pendingAccountCannotActivateBeforeExternalIdentityIsLinked() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                account::activate
        );

        assertTrue(exception.getMessage().contains("Keycloak identity"));
        assertEquals(AccountStatus.PENDING, account.getStatus());
    }

    @Test
    void linkedPendingAccountCanBeActivatedExplicitly() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        account.linkKeycloakSubject("kc-user-001");

        account.activate();

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void disabledAccountCannotBeReactivated() {
        Account account = Account.rehydrate(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001",
                "kc-user-001",
                AccountStatus.DISABLED
        );

        assertThrows(IllegalStateException.class, account::activate);
        assertEquals(AccountStatus.DISABLED, account.getStatus());
    }
}