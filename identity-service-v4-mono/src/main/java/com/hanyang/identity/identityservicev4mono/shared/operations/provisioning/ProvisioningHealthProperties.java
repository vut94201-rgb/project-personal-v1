package com.hanyang.identity.identityservicev4mono.shared.operations.provisioning;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "operations.provisioning-health")
public class ProvisioningHealthProperties {

            @NotNull
    private Duration staleAfter = Duration.ofMinutes(15);

    private boolean providerProbeEnabled = true;

            public Duration getStaleAfter() {
               return staleAfter;    }

            public void setStaleAfter(Duration staleAfter) {
                this.staleAfter = staleAfter;
          }

            public boolean isProviderProbeEnabled() {
                return providerProbeEnabled;
            }

            public void setProviderProbeEnabled(boolean providerProbeEnabled) {
                this.providerProbeEnabled = providerProbeEnabled;
            }
}