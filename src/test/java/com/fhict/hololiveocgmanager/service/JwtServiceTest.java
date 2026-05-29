package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.config.JwtProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    @Test
    void generateAccessTokenAndExtractUsername() {
        JwtService jwtService = new JwtService(jwtProperties("test-secret", 3600));

        String token = jwtService.generateAccessToken("user");

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("user");
    }

    @Test
    void isTokenValidReturnsFalseForExpiredToken() {
        JwtService jwtService = new JwtService(jwtProperties("test-secret", -10));

        String token = jwtService.generateAccessToken("user");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValidReturnsFalseForTamperedToken() {
        JwtService jwtService = new JwtService(jwtProperties("test-secret", 3600));
        String token = jwtService.generateAccessToken("user");

        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    void extractUsernameRejectsInvalidSignature() {
        JwtService jwtService = new JwtService(jwtProperties("test-secret", 3600));
        String token = jwtService.generateAccessToken("user");

        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid JWT signature");
    }

    @Test
    void extractUsernameRejectsInvalidFormat() {
        JwtService jwtService = new JwtService(jwtProperties("test-secret", 3600));

        assertThatThrownBy(() -> jwtService.extractUsername("bad.token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid JWT format");
    }

    private JwtProperties jwtProperties(String secret, long expirationSeconds) {
        JwtProperties properties = new JwtProperties();
        String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
        properties.setSecret(base64Secret);
        properties.setAccessTokenExpirationSeconds(expirationSeconds);
        return properties;
    }
}

