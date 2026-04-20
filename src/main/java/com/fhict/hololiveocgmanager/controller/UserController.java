package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.dto.request.UserCreateRequest;
import com.fhict.hololiveocgmanager.dto.response.UserResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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
    public UserEntity findById(@PathVariable Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @GetMapping("/search")
    public Page<UserEntity> searchUsers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    @PutMapping("/{id}")
    public UserEntity updateUser(@PathVariable Integer id, @RequestBody UserEntity user) {
        user.setId(id);
        return userRepository.save(user);
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
                .passwordHash(request.getPasswordHash())
                .build();
    }
}
