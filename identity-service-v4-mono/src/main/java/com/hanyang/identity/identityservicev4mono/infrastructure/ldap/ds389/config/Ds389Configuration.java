package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config;

import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.account.application.port.DirectoryCredentialPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.account.Ds389AccountAdapter;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.account.Ds389CredentialAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "integration.ds389",
        name = "enabled",
        havingValue = "true"
)
public class Ds389Configuration {

    @Bean(name = "ds389ContextSource")
    public LdapContextSource ds389ContextSource(Ds389Properties properties) {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(requireText(properties.url(), "integration.ds389.url"));
        contextSource.setBase(requireText(properties.baseDn(), "integration.ds389.base-dn"));
        contextSource.setUserDn(requireText(properties.bindDn(), "integration.ds389.bind-dn"));
        contextSource.setPassword(requireText(
                properties.bindPassword(),
                "integration.ds389.bind-password"
        ));
        contextSource.afterPropertiesSet();
        return contextSource;
    }

    @Bean(name = "ds389LdapTemplate")
    public LdapTemplate ds389LdapTemplate(
            @Qualifier("ds389ContextSource") LdapContextSource contextSource
    ) {
        return new LdapTemplate(contextSource);
    }

    @Bean
    public AccountDirectoryPort ds389AccountDirectoryPort(
            @Qualifier("ds389LdapTemplate") LdapTemplate ldapTemplate,
            Ds389Properties properties
    ) {
        return new Ds389AccountAdapter(ldapTemplate, properties);
    }

    @Bean
    public DirectoryCredentialPort ds389DirectoryCredentialPort(
            @Qualifier("ds389LdapTemplate") LdapTemplate ldapTemplate,
            Ds389Properties properties
    ) {
        return new Ds389CredentialAdapter(ldapTemplate, properties);
    }

    private static String requireText(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " must not be blank");
        }
        return value.trim();
    }
}