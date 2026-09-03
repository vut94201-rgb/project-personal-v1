package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;

import com.hanyang.identity.identityservicev4mono.security.revocation.AccessRevocationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        AccessRevocationProperties.class,
        BackChannelLogoutProperties.class
})
public class BackChannelLogoutConfiguration {

    @Bean("backChannelLogoutJwtDecoder")
    JwtDecoder backchannelLogoutJwtDecoder(
            BackChannelLogoutProperties properties
    ) {
        return JwtDecoders.fromIssuerLocation(
                properties.issuer()
        );
    }
}
