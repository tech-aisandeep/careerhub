package com.careerhub.identity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {
    @Bean
    public KeyPair keyPair() {

        try {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);

            return generator.generateKeyPair();

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to generate RSA key pair",
                    exception
            );
        }
    }

    @Bean
    public JwtEncoder jwtEncoder(KeyPair keyPair) {

        return NimbusJwtEncoder.withKeyPair(
                (RSAPublicKey) keyPair.getPublic(),
                (java.security.interfaces.RSAPrivateKey) keyPair.getPrivate()
        ).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(KeyPair keyPair) {

        return org.springframework.security.oauth2.jwt
                .NimbusJwtDecoder
                .withPublicKey(
                        (RSAPublicKey) keyPair.getPublic()
                )
                .build();
    }
}
