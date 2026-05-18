package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.dto.request.UserCreateRequest;
import com.fhict.hololiveocgmanager.dto.request.UserUpdateRequest;
import com.fhict.hololiveocgmanager.dto.response.UserResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.ForbiddenException;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Integer id) {
        User user = userService.getUser(id);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return toResponse(user);
    }

    @GetMapping("/search")
    public Page<UserResponse> searchUsers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);

        return userService.getUsersByUsername(username, pageable)
                .map(this::toResponse);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Integer id, @RequestBody UserUpdateRequest updateRequest) {
        if (updateRequest == null)
        {
            throw new IllegalArgumentException("Update request body is required");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof String username)) {
            throw new ForbiddenException("User is not authenticated");
        }

        Integer authenticatedUserId = userRepository.findByUsername(username)
                .map(UserEntity::getId)
                .orElseThrow(() -> new ForbiddenException("User is not authorized"));

        return toResponse(userService.updateUser(updateRequest, authenticatedUserId));
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        User createdUser = userService.createUser(toDomain(userCreateRequest));

        return toResponse(createdUser);
    }

    @DeleteMapping({"/{id}"})
    public void deleteUser(@PathVariable Integer id)
    {
        userService.deleteUser(id);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .ID(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .build();
    }

    private User toDomain(UserCreateRequest request) {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(request.getPassword())
                .build();
    }
}
