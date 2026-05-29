package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.dto.request.UserCreateRequest;
import com.fhict.hololiveocgmanager.dto.request.UserUpdateRequest;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.GlobalExceptionHandler;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userRepository, userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findByIdReturnsUser() throws Exception {
        when(userService.getUser(7)).thenReturn(User.builder()
                .id(7)
                .username("user7")
                .email("user7@example.com")
                .bio("Bio")
                .build());

        mockMvc.perform(get("/api/users/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ID").value(7))
                .andExpect(jsonPath("$.username").value("user7"))
                .andExpect(jsonPath("$.email").value("user7@example.com"))
                .andExpect(jsonPath("$.bio").value("Bio"));
    }

    @Test
    void findByIdReturnsNotFound() throws Exception {
        when(userService.getUser(99)).thenReturn(null);

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("User not found"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:not-found"));
    }

    @Test
    void searchUsersReturnsPage() throws Exception {
        when(userService.getUsersByUsername(eq("ina"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(User.builder()
                        .id(2)
                        .username("ina")
                        .email("ina@example.com")
                        .build()), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/users/search")
                        .param("username", "ina"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].ID").value(2))
                .andExpect(jsonPath("$.content[0].username").value("ina"));
    }

    @Test
    void updateUserRejectsNullBody() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void updateUserRejectsUnauthenticated() throws Exception {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("newname");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    void updateUserRejectsUnauthorizedUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("newname");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User is not authorized"));
    }

    @Test
    void updateUserReturnsUpdatedResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(5).username("user1").build()));

        when(userService.updateUser(any(UserUpdateRequest.class), eq(5)))
                .thenReturn(User.builder()
                        .id(5)
                        .username("newname")
                        .email("new@example.com")
                        .bio("New bio")
                        .build());

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("newname");
        request.setEmail("new@example.com");
        request.setBio("New bio");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ID").value(5))
                .andExpect(jsonPath("$.username").value("newname"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.bio").value("New bio"));
    }

    @Test
    void createUserRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:bad-request"));
    }

    @Test
    void createUserReturnsResponse() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenReturn(User.builder()
                        .id(10)
                        .username("newuser")
                        .email("newuser@example.com")
                        .bio("Bio")
                        .build());

        UserCreateRequest request = UserCreateRequest.builder()
                .username("newuser")
                .email("newuser@example.com")
                .password("secret")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ID").value(10))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.bio").value("Bio"));
    }

    @Test
    void deleteUserCallsService() throws Exception {
        doNothing().when(userService).deleteUser(4);

        mockMvc.perform(delete("/api/users/4"))
                .andExpect(status().isOk());
    }
}




