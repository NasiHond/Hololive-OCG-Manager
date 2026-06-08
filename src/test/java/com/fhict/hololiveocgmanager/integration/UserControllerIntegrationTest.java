package com.fhict.hololiveocgmanager.integration;

import com.fhict.hololiveocgmanager.dto.request.UserUpdateRequest;
import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.repository.*;
import com.fhict.hololiveocgmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.OutputStream;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtService jwtService;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
        seedTestData();
    }

    private void seedTestData() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("User1");
        user1.setEmail("test@mail.com");
        user1.setBio("a bio");
        user1.setPasswordHash("hashpassword");

        userRepository.save(user1);
    }

    @Test
    void shouldGetUserWithId() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("User1"))
                .andExpect(jsonPath("$.bio").value("a bio"))
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserWithUsername() throws Exception {
        mockMvc.perform(get("/api/users/search?username=User1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnEmptyListForNonExistingUser() throws Exception {
        mockMvc.perform(get("/api/users/search?username=NonExistingUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void shouldReturnUserWithUpdate() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "UpdatedUser",
                                    "email": "UpdatedEmail",
                                    "bio": "UpdatedBio",
                                    "passwordHash": "UpdatedPasswordHash",
                                    "profileImageUrl": "UpdatedProfileImageUrl"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("UpdatedUser"))
                .andExpect(jsonPath("$.email").value("UpdatedEmail"))
                .andExpect(jsonPath("$.bio").value("UpdatedBio"));
    }

    @Test
    void shouldReturnBadRequestForExistingUsername() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());

        // Create another user to have a username conflict
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("AnotherUser");
        anotherUser.setEmail("AnotherEmail");
        anotherUser.setBio("AnotherBio");
        anotherUser.setPasswordHash("AnotherPasswordHash");
        anotherUser.setProfileImageUrl("AnotherProfileImageUrl");
        userRepository.save(anotherUser);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "AnotherUser",
                                    "email": "UpdatedEmail",
                                    "bio": "UpdatedBio",
                                    "passwordHash": "UpdatedPasswordHash",
                                    "profileImageUrl": "UpdatedProfileImageUrl"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForExistingEmail() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());

        // Create another user to have a username conflict
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("AnotherUser");
        anotherUser.setEmail("AnotherEmail");
        anotherUser.setBio("AnotherBio");
        anotherUser.setPasswordHash("AnotherPasswordHash");
        anotherUser.setProfileImageUrl("AnotherProfileImageUrl");
        userRepository.save(anotherUser);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "NewUsername",
                                    "email": "AnotherEmail",
                                    "bio": "UpdatedBio",
                                    "passwordHash": "UpdatedPasswordHash",
                                    "profileImageUrl": "UpdatedProfileImageUrl"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUserWithCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "NewUser",
                                    "email": "NewEmail@mail.com",
                                    "password": "NewPasswordHash"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("NewUser"))
                .andExpect(jsonPath("$.email").value("NewEmail@mail.com"));
    }

    @Test
    void shouldReturnBadRequestForCreateUserWithExistingUsername() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "User1",
                                    "email": "someEmail@mail.com",
                                    "password": "Very secure"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForCreateUserWithExistingEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content("""
                                {
                                    "username": "SomeUsername",
                                    "email": "test@mail.com",
                                    "password": "very secure",
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnGoodRequestForDeleteUser() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());

        mockMvc.perform(delete("/api/users/{id}", user.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedForDeletingUserWhileUnAuthorized() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());

        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("AnotherUser");
        anotherUser.setEmail("AnotherEmail@mail.com");
        anotherUser.setPasswordHash("AnotherPasswordHash");
        userRepository.save(anotherUser);

        mockMvc.perform(delete("/api/users/{id}", anotherUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
