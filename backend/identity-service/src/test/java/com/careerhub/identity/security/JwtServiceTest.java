package com.careerhub.identity.security;

import com.careerhub.identity.entities.Role;
import com.careerhub.identity.entities.User;
import com.careerhub.identity.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() throws Exception {

        KeyPairGenerator generator =
                KeyPairGenerator.getInstance("RSA");

        generator.initialize(2048);

        KeyPair keyPair =
                generator.generateKeyPair();

        JwtEncoder encoder =
                org.springframework.security.oauth2.jwt
                        .NimbusJwtEncoder
                        .withKeyPair(
                                (java.security.interfaces.RSAPublicKey)
                                        keyPair.getPublic(),
                                (java.security.interfaces.RSAPrivateKey)
                                        keyPair.getPrivate()
                        )
                        .build();

        jwtDecoder =
                org.springframework.security.oauth2.jwt
                        .NimbusJwtDecoder
                        .withPublicKey(
                                (java.security.interfaces.RSAPublicKey)
                                        keyPair.getPublic()
                        )
                        .build();

        // 1. Create JwtService first
        jwtService = new JwtService(encoder);

        // 2. Set configuration fields after object creation
        ReflectionTestUtils.setField(
                jwtService,
                "issuer",
                "http://localhost:8081"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "accessTokenExpirationSeconds",
                900L
        );
    }

    @Test
    void shouldGenerateValidJwt() {

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_CANDIDATE")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("candidate@example.com")
                .firstName("Sandeep")
                .enabled(true)
                .build();

        user.getRoles().add(role);

        String token =
                jwtService.generateAccessToken(user);

        assertThat(token)
                .isNotBlank();

        Jwt jwt =
                jwtDecoder.decode(token);

        assertThat(jwt.getSubject())
                .isEqualTo(user.getId().toString());

        assertThat(jwt.getClaimAsString("email"))
                .isEqualTo("candidate@example.com");

        assertThat(jwt.getClaimAsStringList("roles"))
                .contains("ROLE_CANDIDATE");
    }
}
