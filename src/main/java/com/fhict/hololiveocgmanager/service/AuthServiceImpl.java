package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.AuthResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fhict.hololiveocgmanager.exception.UnauthorizedException;

@Service
public class AuthServiceImpl implements AuthService {

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
        UserEntity user;
        if (loginRequest.getIdentifier().contains("@")) {
            user = userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier())
                    .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
        } else {
            user = userRepository.findByUsername(loginRequest.getIdentifier())
                    .orElseThrow(() -> new UnauthorizedException("Invalid username/email or password"));
        }

        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
        if (!matches) {
            throw new UnauthorizedException("Invalid username/email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername());

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
