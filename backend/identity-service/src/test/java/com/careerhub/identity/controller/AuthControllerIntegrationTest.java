package com.careerhub.identity.controller;

import com.careerhub.identity.config.SecurityConfig;
import com.careerhub.identity.exception.GlobalExceptionHandler;
import com.careerhub.identity.exception.UserAlreadyExistException;
import com.careerhub.identity.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldReturn409WhenUserAlreadyExists() throws Exception {

        when(authService.register(any()))
                .thenThrow(
                        new UserAlreadyExistException(
                                "candidate@example.com"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                //.with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "candidate@example.com",
                                      "password": "Password@123",
                                      "firstName": "Sandeep",
                                      "lastName": "Kumar"
                                    }
                                """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/auth/register"));
    }

    @Test
    void shouldReturn400ForInvalidRequest() throws Exception {

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "invalid-email",
                                      "password": "123",
                                      "firstName": ""
                                    }
                                """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"));
    }
}
