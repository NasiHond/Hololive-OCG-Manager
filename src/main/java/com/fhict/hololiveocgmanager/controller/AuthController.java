package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.LoginResponse;
import com.fhict.hololiveocgmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) throws AuthenticationException {
        LoginResponse loginResponse = authService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<LoginResponse> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse.builder()
                        .tokenType("Bearer")
                        .message(ex.getMessage())
                        .authenticated(false)
                        .build());
    }
}
