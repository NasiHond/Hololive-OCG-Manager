package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.config.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String JWT_TYPE_HEADER = base64UrlEncodeStatic("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
    private static final Pattern SUB_PATTERN = Pattern.compile("\\\"sub\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"");
    private static final Pattern EXP_PATTERN = Pattern.compile("\\\"exp\\\"\\s*:\\s*(\\d+)");

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(String username) {
        long issuedAt = Instant.now().getEpochSecond();
        long expiresAt = issuedAt + jwtProperties.getAccessTokenExpirationSeconds();

        String payloadJson = "{\"sub\":\"" + escapeJson(username) + "\",\"iat\":" + issuedAt + ",\"exp\":" + expiresAt + "}";
        String encodedPayload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = JWT_TYPE_HEADER + "." + encodedPayload;
        String signature = base64UrlEncode(sign(signingInput));
        return signingInput + "." + signature;
    }

    public String extractUsername(String token) {
        String payloadJson = validateAndReadPayloadJson(token);
        Matcher subMatcher = SUB_PATTERN.matcher(payloadJson);
        if (!subMatcher.find()) {
            throw new IllegalArgumentException("Token does not contain subject");
        }
        return unescapeJson(subMatcher.group(1));
    }

    public boolean isTokenValid(String token) {
        try {
            String payloadJson = validateAndReadPayloadJson(token);
            Matcher expMatcher = EXP_PATTERN.matcher(payloadJson);
            if (!expMatcher.find()) {
                return false;
            }
            long exp = Long.parseLong(expMatcher.group(1));
            return exp > Instant.now().getEpochSecond();
        } catch (Exception ex) {
            return false;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationSeconds();
    }

    private String validateAndReadPayloadJson(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        String signingInput = parts[0] + "." + parts[1];
        String expectedSignature = base64UrlEncode(sign(signingInput));
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
        return new String(payloadBytes, StandardCharsets.UTF_8);
    }

    private byte[] sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(getSigningKeyBytes(), HMAC_ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign JWT token", ex);
        }
    }

    private String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static String base64UrlEncodeStatic(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String unescapeJson(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private byte[] getSigningKeyBytes() {
        return Base64.getDecoder().decode(jwtProperties.getSecret());
    }
}

