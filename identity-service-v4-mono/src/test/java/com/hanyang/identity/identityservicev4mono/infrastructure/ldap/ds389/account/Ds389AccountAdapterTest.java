package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.account;

import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.Name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Ds389AccountAdapterTest {

    @Test
    void pendingDirectoryAccountIsCreatedLockedUnderPeopleOu() {
        LdapTemplate ldapTemplate = mock(LdapTemplate.class);
        Ds389Properties properties = new Ds389Properties(
                true,
                "ldap://localhost:3389",
                "dc=hanyang,dc=local",
                "cn=Directory Manager",
                "change_me",
                "ou=People",
                false
        );

        when(ldapTemplate.lookup(
                any(Name.class),
                any(String[].class),
                any(ContextMapper.class)
        )).thenThrow(new NameNotFoundException("missing"));

        Ds389AccountAdapter adapter = new Ds389AccountAdapter(
                ldapTemplate,
                properties
        );

        AccountDirectoryPort.DirectoryAccount result = adapter.ensureAccount(
                new AccountDirectoryPort.DirectoryAccountSpec(
                        " emp001 ",
                        " E000001 ",
                        " Test User ",
                        null,
                        " test001@hanyang.local ",
                        false
                )
        );

        ArgumentCaptor<DirContextOperations> contextCaptor =
                ArgumentCaptor.forClass(DirContextOperations.class);
        verify(ldapTemplate).bind(contextCaptor.capture());

        DirContextOperations context = contextCaptor.getValue();
        assertEquals("emp001", context.getStringAttribute("uid"));
        assertEquals("E000001", context.getStringAttribute("employeeNumber"));
        assertEquals("Test User", context.getStringAttribute("cn"));
        assertEquals("Test User", context.getStringAttribute("sn"));
        assertEquals("test001@hanyang.local", context.getStringAttribute("mail"));
        assertEquals("true", context.getStringAttribute("nsAccountLock"));

        assertEquals("emp001", result.username());
        assertEquals(
                "uid=emp001,ou=People,dc=hanyang,dc=local",
                result.externalDn()
        );
        assertEquals(false, result.authenticationAllowed());
    }
}