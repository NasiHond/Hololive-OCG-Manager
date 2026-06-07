package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.AuthResponse;
import com.fhict.hololiveocgmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "{app.cors.allowed-origins}", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String token) {
        AuthResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }
}
