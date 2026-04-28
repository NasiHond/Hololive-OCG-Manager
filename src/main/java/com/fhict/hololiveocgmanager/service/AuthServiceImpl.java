package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.LoginResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;

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
    public LoginResponse login(LoginRequest loginRequest) throws AuthenticationException {
        UserEntity user;
        if (loginRequest.getIdentifier().contains("@")) {
            user = userRepository.findByEmailIgnoreCase(loginRequest.getIdentifier()).orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));
        } else {
            user = userRepository.findByUsername(loginRequest.getIdentifier()).orElseThrow(() -> new AuthenticationException("Invalid username/email or password"));
        }

        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash());
        if (!matches) {
            throw new AuthenticationException("Invalid username/email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername());

        return LoginResponse.builder()
                .username(user.getUsername())
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(String.valueOf(jwtService.getAccessTokenExpirationSeconds()))
                .message("Login successful")
                .authenticated(true)
                .build();
    }
}
