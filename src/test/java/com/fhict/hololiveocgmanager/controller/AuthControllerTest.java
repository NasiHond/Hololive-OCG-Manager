package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.LoginRequest;
import com.fhict.hololiveocgmanager.dto.response.AuthResponse;
import com.fhict.hololiveocgmanager.exception.GlobalExceptionHandler;
import com.fhict.hololiveocgmanager.exception.UnauthorizedException;
import com.fhict.hololiveocgmanager.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void loginReturnsAuthResponse() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .identifier("user@example.com")
                .password("secret")
                .build();
        AuthResponse response = AuthResponse.builder()
                .accessToken("access-token")
                .tokenType("Bearer")
                .expiresIn("3600")
                .username("user")
                .id(12)
                .message("Login successful")
                .authenticated(true)
                .valid(true)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value("3600"))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void loginRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:bad-request"));
    }

    @Test
    void loginRejectsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid username/email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder().identifier("user").password("bad").build())))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid username/email or password"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"));
    }

    @Test
    void validateTokenReturnsAuthResponse() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .username("user")
                .authenticated(true)
                .valid(true)
                .tokenType("Bearer")
                .message("Token is valid")
                .build();

        when(authService.validateToken("Bearer token")).thenReturn(response);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.authenticated").value(true))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.message").value("Token is valid"));
    }

    @Test
    void validateTokenRejectsUnauthorized() throws Exception {
        when(authService.validateToken("Bearer bad"))
                .thenThrow(new UnauthorizedException("Invalid or expired token"));

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.detail").value("Invalid or expired token"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:unauthorized"));
    }
}



