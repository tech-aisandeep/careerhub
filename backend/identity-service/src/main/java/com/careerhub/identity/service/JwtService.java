package com.careerhub.identity.service;

import ch.qos.logback.classic.spi.ConfiguratorRank;
import com.careerhub.identity.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
//@ConfigurationProperties
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${careerhub.security.jwt.issuer:http://localhost:8081}")
    private String issuer;

    @Value("${careerhub.security.jwt.access-token-expiration:900}")
    private long accessTokenExpirationSeconds;

    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getName())
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(accessTokenExpirationSeconds)
                )
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }
}
