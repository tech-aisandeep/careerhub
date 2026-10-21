package com.careerhub.identity.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.jwt;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnCurrentUser() throws Exception {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("12345678-1234-1234-1234-123456789012")
                .claim("email", "candidate@example.com")
                .claim(
                        "roles",
                        java.util.List.of("ROLE_CANDIDATE")
                )
                .build();

        mockMvc.perform(
                        get("/api/v1/users/me")
                                .with(jwt().jwt(jwt))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.email")
                                .value("candidate@example.com")
                )
                .andExpect(
                        jsonPath("$.roles[0]")
                                .value("ROLE_CANDIDATE")
                );
    }

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {

        mockMvc.perform(
                        get("/api/v1/users/me")
                )
                .andExpect(status().isUnauthorized());
    }
}