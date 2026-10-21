package com.careerhub.identity;

import com.careerhub.identity.dto.RegisterRequest;
import com.careerhub.identity.dto.UserResponse;
import com.careerhub.identity.entities.User;
import com.careerhub.identity.repo.UserRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;

import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestRestTemplate
public class IdentityServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("identity_test_db")
                    .withUsername("test_user")
                    .withPassword("test_password");

    @LocalServerPort
    private int port;

    @Autowired(required = true)
    private TestRestTemplate restTemplate;

    @Autowired(required = true)
    private UserRespository userRespository;

    @BeforeEach
    void cleanDatabase() {
        userRespository.deleteAll();
    }

    @Test
    void shouldRegisterCandidateSuccessfully() {

        // Arrange
        RegisterRequest request = new RegisterRequest(
                "integration1@example.com",
                "Password@123",
                "Sandeep",
                "Kumar"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterRequest> entity =
                new HttpEntity<>(request, headers);

        // Act
        ResponseEntity<UserResponse> response =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/api/v1/auth/register",
                        entity,
                        UserResponse.class
                );

        // Assert
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().email())
                .isEqualTo("integration1@example.com");

        assertThat(response.getBody().firstName())
                .isEqualTo("Sandeep");

        assertThat(response.getBody().lastName())
                .isEqualTo("Kumar");

        User user = userRespository
                .findByEmailIgnoreCase("integration1@example.com")
                .orElseThrow();

        assertThat(user.getEmail())
                .isEqualTo("integration1@example.com");

        assertThat(user.getPasswordHash())
                .isNotEqualTo("Password@123");

        assertThat(user.getRoles())
                .anyMatch(role ->
                        role.getName().equals("ROLE_CANDIDATE"));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request = new RegisterRequest(
                "duplicate1@example.com",
                "Password@123",
                "Sandeep",
                "Kumar"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegisterRequest> entity =
                new HttpEntity<>(request, headers);

        // First registration
        ResponseEntity<String> firstResponse =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/api/v1/auth/register",
                        entity,
                        String.class
                );

        assertThat(firstResponse.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        // Second registration
        ResponseEntity<String> secondResponse =
                restTemplate.postForEntity(
                        "http://localhost:" + port + "/api/v1/auth/register",
                        entity,
                        String.class
                );

        assertThat(secondResponse.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
    }
}
