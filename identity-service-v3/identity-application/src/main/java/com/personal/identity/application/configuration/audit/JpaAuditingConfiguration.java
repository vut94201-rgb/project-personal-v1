package com.personal.identity.application.configuration.audit;

import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware")
public class JpaAuditingConfiguration {

  @Bean
  AuditorAware<String> securityAuditorAware() {

    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (Objects.isNull(authentication)
          || !authentication.isAuthenticated()
          || authentication instanceof AnonymousAuthenticationToken) {
        return Optional.empty();
      }
      return Optional.ofNullable(authentication.getName());
    };
  }
}
