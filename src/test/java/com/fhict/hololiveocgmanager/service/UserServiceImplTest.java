package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.request.UserUpdateRequest;
import com.fhict.hololiveocgmanager.entity.CollectionEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import com.fhict.hololiveocgmanager.repository.CollectionRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserRejectsInvalidUser() {
        User invalid = User.builder().username("user").build();

        assertThatThrownBy(() -> userService.createUser(invalid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Account must have a username, email and password.");
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        User user = User.builder()
                .username("user")
                .email("user@example.com")
                .passwordHash("secret")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("user")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already taken.");
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        User user = User.builder()
                .username("user")
                .email("user@example.com")
                .passwordHash("secret")
                .build();

        when(userRepository.existsByUsernameIgnoreCase("user")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already taken.");
    }

    @Test
    void createUserCreatesDefaultCollection() {
        User user = User.builder()
                .username("user")
                .email("user@example.com")
                .passwordHash("secret")
                .build();
        UserEntity toSave = UserEntity.builder()
                .username("user")
                .email("user@example.com")
                .passwordHash("hashed")
                .build();
        UserEntity saved = UserEntity.builder()
                .id(10)
                .username("user")
                .email("user@example.com")
                .passwordHash("hashed")
                .build();
        User domain = User.builder().id(10).username("user").email("user@example.com").build();

        when(userRepository.existsByUsernameIgnoreCase("user")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userMapper.toEntity(any(User.class))).thenReturn(toSave);
        when(userRepository.save(toSave)).thenReturn(saved);
        when(userMapper.toDomain(saved)).thenReturn(domain);

        User created = userService.createUser(user);

        assertThat(created.getId()).isEqualTo(10);
        assertThat(created.getUsername()).isEqualTo("user");

        ArgumentCaptor<CollectionEntity> captor = ArgumentCaptor.forClass(CollectionEntity.class);
        verify(collectionRepository).save(captor.capture());
        CollectionEntity collection = captor.getValue();
        assertThat(collection.getOwnerId()).isEqualTo(saved);
        assertThat(collection.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void getUserReturnsDomainUser() {
        UserEntity entity = UserEntity.builder().id(3).username("user").build();
        User domain = User.builder().id(3).username("user").build();

        when(userRepository.findById(3)).thenReturn(Optional.of(entity));
        when(userMapper.toDomain(entity)).thenReturn(domain);

        assertThat(userService.getUser(3)).isEqualTo(domain);
    }

    @Test
    void getUserReturnsNullWhenMissing() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());
        when(userMapper.toDomain(null)).thenReturn(null);

        assertThat(userService.getUser(99)).isNull();
    }

    @Test
    void getUsersByUsernameReturnsPage() {
        UserEntity entity = UserEntity.builder().id(2).username("ina").build();
        User domain = User.builder().id(2).username("ina").build();

        when(userRepository.findByUsernameContainingIgnoreCase(eq("ina"), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(userMapper.toDomain(entity)).thenReturn(domain);

        assertThat(userService.getUsersByUsername("ina", PageRequest.of(0, 20))
                .getContent()).containsExactly(domain);
    }

    @Test
    void updateUserRejectsMissingUser() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("new");

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(request, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User with id 1 not found.");
    }

    @Test
    void updateUserRejectsDuplicateUsername() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("dup");
        UserEntity existing = UserEntity.builder().id(1).username("old").build();
        UserEntity other = UserEntity.builder().id(2).username("dup").build();

        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsernameIgnoreCase("dup")).thenReturn(true);
        when(userRepository.findByUsernameIgnoreCase("dup")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(request, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username is already taken.");
    }

    @Test
    void updateUserRejectsDuplicateEmail() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("dup@example.com");
        UserEntity existing = UserEntity.builder().id(1).username("old").build();
        UserEntity other = UserEntity.builder().id(2).email("dup@example.com").build();

        when(userRepository.findById(1)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmailIgnoreCase("dup@example.com")).thenReturn(true);
        when(userRepository.findByEmailIgnoreCase("dup@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.updateUser(request, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already taken.");
    }

    @Test
    void updateUserUpdatesFields() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("new");
        request.setEmail("new@example.com");
        request.setPasswordHash("secret");
        request.setBio("Bio");
        request.setProfileImageUrl("/img.png");
        UserEntity entity = UserEntity.builder().id(1).username("old").build();
        UserEntity saved = UserEntity.builder().id(1).username("new").email("new@example.com").build();
        User domain = User.builder().id(1).username("new").email("new@example.com").build();

        when(userRepository.findById(1)).thenReturn(Optional.of(entity));
        when(userRepository.existsByUsernameIgnoreCase("new")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDomain(saved)).thenReturn(domain);

        User updated = userService.updateUser(request, 1);

        assertThat(updated).isEqualTo(domain);
        assertThat(entity.getUsername()).isEqualTo("new");
        assertThat(entity.getEmail()).isEqualTo("new@example.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hashed");
        assertThat(entity.getBio()).isEqualTo("Bio");
        assertThat(entity.getProfileImageUrl()).isEqualTo("/img.png");
    }

    @Test
    void deleteUserDeletesById() {
        userService.deleteUser(5);

        verify(userRepository).deleteById(5);
    }
}
