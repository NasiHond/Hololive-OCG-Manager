package com.fhict.hololiveocgmanager.integration;

import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class AuthControllerIntegrationTest {
    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtService jwtService;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
        seedTestData();
    }

    private void seedTestData() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("testUser");
        userEntity.setEmail("test@mail.com");
        userEntity.setPasswordHash("$2a$12$kbhl32F6fGL6jCfGploD9u0AtHZw.G7yC7D6VTMXIoU4dgntOzRRK");//"testing" hashed with Bcrypt
        userRepository.save(userEntity);
    }

    @Test
    void shouldReturnAuthResponseWhenLoginWithUsername() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                        {
                            "identifier": "testUser",
                            "password": "testing"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.username").value("testUser"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void shouldReturnUnauthorizedWhenLoginWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                        {
                            "identifier": "testUser",
                            "password": "wrongPassword"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username/email or password"));
    }

    @Test
    void shouldReturnAuthResponseWhenValidate() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());

        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("testUser"));
    }

    @Test
    void shouldReturnUnauthorizedWhenValidateWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "Bearer " + "invalidToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid or expired token"));
    }

    @Test
    void shouldReturnUnauthorizedWhenValidateWithWrongFormat() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                .header("Authorization", "WrongFormatToken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid authorization header format"));
    }
}
