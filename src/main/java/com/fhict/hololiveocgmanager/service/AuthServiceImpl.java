package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.AuthResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fhict.hololiveocgmanager.exception.UnauthorizedException;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthServiceImpl(BCryptPasswordEncoder passwordEncoder, UserRepository userRepository, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Login attempt with identifier: {}", loginRequest.getIdentifier());

            UserEntity user;
            if (loginRequest.getIdentifier().contains("@")) {
                logger.info("Looking up user by email");
                user = userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier())
                        .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
            } else {
                logger.info("Looking up user by username");
                user = userRepository.findByUsername(loginRequest.getIdentifier())
                        .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
            }

            logger.info("User found: {}", user.getUsername());

            boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
            if (!matches) {
                logger.warn("Password mismatch for user: {}", user.getUsername());
                throw new UnauthorizedException("Invalid username/email or password");
            }

            logger.info("Password matched, generating JWT token");
            String accessToken = jwtService.generateAccessToken(user.getUsername());
            logger.info("Token generated successfully");

            return AuthResponse.builder()
                    .username(user.getUsername())
                    .id(user.getId())
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(String.valueOf(jwtService.getAccessTokenExpirationSeconds()))
                    .message("Login successful")
                    .authenticated(true)
                    .valid(true)
                    .build();
        } catch (Exception ex) {
            logger.error("Login failed with exception", ex);
            throw ex;
        }
    }

    @Override
    public AuthResponse validateToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header format");
        }

        String jwt = token.substring(7); // Remove "Bearer " prefix

        if (!jwtService.isTokenValid(jwt)) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        String username = jwtService.extractUsername(jwt);

        return AuthResponse.builder()
                .username(username)
                .authenticated(true)
                .valid(true)
                .tokenType("Bearer")
                .message("Token is valid")
                .build();
    }
}
