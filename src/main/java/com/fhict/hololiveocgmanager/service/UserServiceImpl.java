package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(User user) {
        if (!user.isValidForCreate())
        {
            throw new IllegalArgumentException("Account must have a username, email and password.");
        }

        if (userRepository.existsByUsernameIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (userRepository.existsByEmailIgnoreCase(user.getEmail())) {
            throw new IllegalArgumentException("Email is already taken.");
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        UserEntity entityToSave = userMapper.toEntity(user);
        UserEntity savedEntity = userRepository.save(entityToSave);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public User getUser(Integer id) {
        return userMapper.toDomain(userRepository.findById(id).orElse(null));
    }

    @Override
    public List<User> getUsersByUsername(String username) {
        List<User> users = new ArrayList<>();
        userRepository.findByUsernameContainingIgnoreCase(username, null).forEach(entity -> users.add(userMapper.toDomain(entity)));
        return users;
    }

    @Override
    @Transactional
    public User updateUser(User user, Integer id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " not found."));
        if (user.getUsername() != null)
        {
            if (userRepository.existsByUsernameIgnoreCase(user.getUsername()) && !userRepository.findByUsernameIgnoreCase(user.getUsername()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Username is already taken.");
            }
            userEntity.setUsername(user.getUsername());
        }
        if (user.getEmail() != null)
        {
            if (userRepository.existsByEmailIgnoreCase(user.getEmail()) && !userRepository.findByEmailIgnoreCase(user.getEmail()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Email is already taken.");
            }
            userEntity.setEmail(user.getEmail());
        }
        if (user.getPasswordHash() != null)
        {
            userEntity.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }
        if (user.getBio() != null)
        {
            userEntity.setBio(user.getBio());
        }

        UserEntity savedEntity = userRepository.save(userEntity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteUser(Integer id)
    {
        userRepository.deleteById(id);
    }
}
