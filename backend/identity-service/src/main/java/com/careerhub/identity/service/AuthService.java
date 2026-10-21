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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;


@Service
@Slf4j
//@RequiredArgsConstructor
public class AuthService {

    private final String DEFAULT_ROLE = "ROLE_CANDIDATE";

    @Autowired
    private UserRespository userRespository;
    @Autowired
    private RoleRespository roleRespository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private JwtService jwtService;

    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {

        String normalizedEmail = registerRequest.email().trim().toLowerCase(Locale.ROOT);
        log.info("Registering user with email={}", normalizedEmail);

        if (userRespository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new UserAlreadyExistException(normalizedEmail);
        }

        Role candidateRole = roleRespository.findByName(DEFAULT_ROLE).orElseThrow(
                () -> new IllegalStateException("Default Role not configured")
        );

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(registerRequest.password()))
                .firstName(registerRequest.firstName().trim())
                .lastName(registerRequest.lastName() == null ? null : registerRequest.lastName().trim())
                .enabled(true)
                .build();
        user.getRoles().add(candidateRole);

        User savedUser = userRespository.save(user);
        log.info("User registered successfully userId={}", savedUser.getId());

        return new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName());
    }
    @Transactional
    public TokenResponse login(LoginRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        User user = userRespository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash())) {

            throw new InvalidCredentialsException();
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                refreshTokenService.create(user);

        log.info(
                "Successful authentication userId={}",
                user.getId()
        );

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds()
        );
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {

        RefreshToken existing =
                refreshTokenService.validate(
                        rawRefreshToken
                );

        User user = existing.getUser();

        refreshTokenService.revoke(existing);

        String accessToken =
                jwtService.generateAccessToken(user);

        String newRefreshToken =
                refreshTokenService.create(user);

        return new TokenResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationSeconds()
        );
}
}
