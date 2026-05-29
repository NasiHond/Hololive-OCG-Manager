package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.AuthResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.UnauthorizedException;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void loginWithEmailReturnsAuthResponse() {
        LoginRequest request = LoginRequest.builder()
                .identifier("user@example.com")
                .password("secret")
                .build();
        UserEntity entity = UserEntity.builder()
                .id(1)
                .username("user")
                .email("user@example.com")
                .passwordHash("hash")
                .build();

        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken("user")).thenReturn("token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("token");
        assertThat(response.getUsername()).isEqualTo("user");
        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getExpiresIn()).isEqualTo("3600");
        assertThat(response.getMessage()).isEqualTo("Login successful");
        assertThat(response.getAuthenticated()).isTrue();
        assertThat(response.getValid()).isTrue();
    }

    @Test
    void loginWithUsernameReturnsAuthResponse() {
        LoginRequest request = LoginRequest.builder()
                .identifier("user")
                .password("secret")
                .build();
        UserEntity entity = UserEntity.builder()
                .id(2)
                .username("user")
                .passwordHash("hash")
                .build();

        when(userRepository.findByUsername("user"))
                .thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken("user")).thenReturn("token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(request);

        assertThat(response.getId()).isEqualTo(2);
        assertThat(response.getUsername()).isEqualTo("user");
    }

    @Test
    void loginRejectsMissingUser() {
        LoginRequest request = LoginRequest.builder()
                .identifier("missing")
                .password("secret")
                .build();

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username/email or password");
    }

    @Test
    void loginRejectsInvalidPassword() {
        LoginRequest request = LoginRequest.builder()
                .identifier("user")
                .password("bad")
                .build();
        UserEntity entity = UserEntity.builder()
                .id(3)
                .username("user")
                .passwordHash("hash")
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid username/email or password");
    }

    @Test
    void validateTokenRejectsMissingBearerPrefix() {
        assertThatThrownBy(() -> authService.validateToken("token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid authorization header format");
    }

    @Test
    void validateTokenRejectsInvalidToken() {
        when(jwtService.isTokenValid("badtoken")).thenReturn(false);

        assertThatThrownBy(() -> authService.validateToken("Bearer badtoken"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    void validateTokenReturnsAuthResponse() {
        when(jwtService.isTokenValid("goodtoken")).thenReturn(true);
        when(jwtService.extractUsername("goodtoken")).thenReturn("user");

        AuthResponse response = authService.validateToken("Bearer goodtoken");

        assertThat(response.getUsername()).isEqualTo("user");
        assertThat(response.getAuthenticated()).isTrue();
        assertThat(response.getValid()).isTrue();
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getMessage()).isEqualTo("Token is valid");
    }
}

