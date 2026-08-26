package com.hanyang.identity.identityservicev4mono.account.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void newAccountStartsPending() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );

        assertEquals(AccountStatus.PENDING, account.getStatus());
    }

    @Test
    void pendingAccountCanBeActivatedByCoordinator() {
        Account account = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );

        account.activate();

        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void disabledAccountCannotBeReactivated() {
        Account account = Account.rehydrate(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001",
                AccountStatus.DISABLED
        );

        assertThrows(IllegalStateException.class, account::activate);
        assertEquals(AccountStatus.DISABLED, account.getStatus());
    }
}