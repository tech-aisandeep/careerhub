package com.careerhub.identity.service;

import com.careerhub.identity.dto.LoginRequest;
import com.careerhub.identity.dto.RegisterRequest;
import com.careerhub.identity.dto.TokenResponse;
import com.careerhub.identity.dto.UserResponse;
import com.careerhub.identity.entities.RefreshToken;
import com.careerhub.identity.entities.Role;
import com.careerhub.identity.entities.User;
import com.careerhub.identity.exception.InvalidCredentialsException;
import com.careerhub.identity.exception.UserAlreadyExistException;
import com.careerhub.identity.repo.RoleRespository;
import com.careerhub.identity.repo.UserRespository;
import com.careerhub.identity.security.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRespository userRespository;

    @Mock
    private RoleRespository roleRespository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private JwtService jwtService;

    @Test
    void shouldRegisterCandidateSuccessfully(){
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "  candidate1@example.com ",
                "Password@123",
                " Sandeep ",
                " Kumaar "
        );

        Role candidateRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_CANDIDATE")
                .build();

        UUID userId = UUID.randomUUID();

        when(userRespository.existsByEmailIgnoreCase("candidate1@example.com"))
                .thenReturn(false);



        when(roleRespository.findByName("ROLE_CANDIDATE"))
                .thenReturn(Optional.of(candidateRole));

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("bcrypt-hashed-password");

        User savedUser = User.builder()
                .id(userId)
                .email("candidate1@example.com")
                .passwordHash("bcrypt-hashed-password")
                .firstName("Sandeep")
                .lastName("Kumaar")
                .enabled(true)
                .build();

        when(userRespository.save(any(User.class)))
                .thenReturn(savedUser);

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("candidate1@example.com");
        assertThat(response.firstName()).isEqualTo("Sandeep");
        assertThat(response.lastName()).isEqualTo("Kumaar");

        verify(userRespository)
                .existsByEmailIgnoreCase("candidate1@example.com");

        verify(roleRespository)
                .findByName("ROLE_CANDIDATE");

        verify(passwordEncoder)
                .encode("Password@123");

        verify(userRespository)
                .save(any(User.class));

    }

    @Test
    void shouldRejectDuplicateEmail(){
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "candidate1@example.com",
                "Password@123",
                "Sandeep",
                "Kumar"
        );

        when(userRespository.existsByEmailIgnoreCase("candidate1@example.com"))
                .thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessageContaining("candidate1@example.com");

        verify(userRespository)
                .existsByEmailIgnoreCase("candidate1@example.com");

        verify(roleRespository, never())
                .findByName(anyString());

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRespository, never())
                .save(any(User.class));
    }

    @Test
    void shouldFailWhenDefaultRoleDoesNotExist(){
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "candidate1@example.com",
                "Password@123",
                "Sandeep",
                "Kumar"
        );

        when(userRespository.existsByEmailIgnoreCase("candidate1@example.com"))
                .thenReturn(false);

        when(roleRespository.findByName("ROLE_CANDIDATE"))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Default Role not configured");

        verify(userRespository, never())
                .save(any(User.class));

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void shouldStoreEncodedPassword(){
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "candidate@example.com",
                "Password@123",
                "Sandeep",
                "Kumar"
        );

        Role candidateRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_CANDIDATE")
                .build();

        when(userRespository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(false);

        when(roleRespository.findByName("ROLE_CANDIDATE"))
                .thenReturn(Optional.of(candidateRole));

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("encoded-password");

        when(userRespository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.register(request);

        // Assert
        verify(userRespository).save(argThat(user ->
                user.getPasswordHash().equals("encoded-password")
                        && !user.getPasswordHash().equals("Password@123")
        ));
    }

    @Test
    void shouldAssignCandidateRole(){
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "candidate1@example.com",
                "Password@123",
                "Sandeep",
                "Kumar"
        );

        Role candidateRole = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_CANDIDATE")
                .build();

        when(userRespository.existsByEmailIgnoreCase(anyString()))
                .thenReturn(false);

        when(roleRespository.findByName("ROLE_CANDIDATE"))
                .thenReturn(Optional.of(candidateRole));

        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded-password");

        when(userRespository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.register(request);

        // Assert
        verify(userRespository).save(argThat(user ->
                user.getRoles().contains(candidateRole)
        ));
    }

    @Test
    void shouldLoginSuccessfully() {

        // Arrange
        LoginRequest request = new LoginRequest(
                "candidate@example.com",
                "Password@123"
        );

        Role role = Role.builder()
                .id(UUID.randomUUID())
                .name("ROLE_CANDIDATE")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("candidate@example.com")
                .passwordHash("bcrypt-hash")
                .firstName("Sandeep")
                .lastName("Kumar")
                .enabled(true)
                .build();

        user.getRoles().add(role);

        when(userRespository.findByEmailIgnoreCase(
                "candidate@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "bcrypt-hash"))
                .thenReturn(true);

        when(jwtService.generateAccessToken(user))
                .thenReturn("access-token");

        when(jwtService.getAccessTokenExpirationSeconds())
                .thenReturn(900L);

        when(refreshTokenService.create(user))
                .thenReturn("refresh-token");

        // Act
        TokenResponse response =
                authService.login(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.accessToken())
                .isEqualTo("access-token");
        assertThat(response.refreshToken())
                .isEqualTo("refresh-token");
        assertThat(response.tokenType())
                .isEqualTo("Bearer");
        assertThat(response.expiresIn())
                .isEqualTo(900);

        verify(passwordEncoder)
                .matches("Password@123", "bcrypt-hash");

        verify(jwtService)
                .generateAccessToken(user);

        verify(refreshTokenService)
                .create(user);
    }

    @Test
    void shouldRejectInvalidPassword() {

        LoginRequest request = new LoginRequest(
                "candidate@example.com",
                "WrongPassword"
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("candidate@example.com")
                .passwordHash("bcrypt-hash")
                .enabled(true)
                .build();

        when(userRespository.findByEmailIgnoreCase(
                "candidate@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "bcrypt-hash"))
                .thenReturn(false);

        assertThatThrownBy(() ->
                authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never())
                .generateAccessToken(any());

        verify(refreshTokenService, never())
                .create(any());
    }

    @Test
    void shouldRejectUnknownUser() {

        LoginRequest request = new LoginRequest(
                "unknown@example.com",
                "Password@123"
        );

        when(userRespository.findByEmailIgnoreCase(
                "unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateAccessToken(any());
    }

    @Test
    void shouldRejectDisabledUser() {

        LoginRequest request = new LoginRequest(
                "disabled@example.com",
                "Password@123"
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("disabled@example.com")
                .passwordHash("bcrypt-hash")
                .enabled(false)
                .build();

        when(userRespository.findByEmailIgnoreCase(
                "disabled@example.com"))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() ->
                authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateAccessToken(any());
    }

    @Test
    void shouldRotateRefreshToken() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("candidate@example.com")
                .passwordHash("bcrypt-hash")
                .firstName("Sandeep")
                .enabled(true)
                .build();

        RefreshToken oldToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .tokenHash("old-hash")
                .expiresAt(
                        java.time.Instant.now()
                                .plusSeconds(1000)
                )
                .revoked(false)
                .build();

        when(refreshTokenService.validate("old-refresh-token"))
                .thenReturn(oldToken);

        when(jwtService.generateAccessToken(user))
                .thenReturn("new-access-token");

        when(refreshTokenService.create(user))
                .thenReturn("new-refresh-token");

        when(jwtService.getAccessTokenExpirationSeconds())
                .thenReturn(900L);

        TokenResponse response =
                authService.refresh("old-refresh-token");

        assertThat(response.accessToken())
                .isEqualTo("new-access-token");

        assertThat(response.refreshToken())
                .isEqualTo("new-refresh-token");

        verify(refreshTokenService)
                .revoke(oldToken);

        verify(refreshTokenService)
                .create(user);
    }


}