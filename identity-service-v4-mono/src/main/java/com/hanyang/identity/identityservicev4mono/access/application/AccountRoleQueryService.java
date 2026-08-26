package com.hanyang.identity.identityservicev4mono.access.application;


import com.hanyang.identity.identityservicev4mono.access.domain.AccountRoleRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleRepository;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
@IdentityReadAccess
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountRoleQueryService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;

    public List<Role> getRoles(AccountId accountId) {
        if (accountRepository.findById(accountId).isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        return roleRepository
                .findAllByIds(
                        accountRoleRepository
                                .findRoleIdsByAccountId(accountId)
                )
                .stream()
                .sorted(Comparator.comparing(Role::getCode))
                .toList();
    }



}