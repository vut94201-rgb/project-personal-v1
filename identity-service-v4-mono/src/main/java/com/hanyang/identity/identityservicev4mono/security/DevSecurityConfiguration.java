package com.hanyang.identity.identityservicev4mono.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
@Profile("!keycloak")
public class DevSecurityConfiguration {

            @Bean
            SecurityFilterChain devSecurityFilterChain(
            HttpSecurity http
    ) throws Exception {
    http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(authorize ->
                                        authorize.anyRequest().permitAll()
                                    );

                        return http.build();
            }
}