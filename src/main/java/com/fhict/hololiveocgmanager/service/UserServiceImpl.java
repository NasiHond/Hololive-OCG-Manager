package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.request.UserUpdateRequest;
import com.fhict.hololiveocgmanager.entity.CollectionEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import com.fhict.hololiveocgmanager.repository.CollectionRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, UserRepository userRepository, CollectionRepository collectionRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.collectionRepository = collectionRepository;
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

        // Create default collection for user
        CollectionEntity collectionEntity = new CollectionEntity();
        collectionEntity.setOwnerId(savedEntity);
        collectionEntity.setVisibility(Visibility.PUBLIC);
        collectionRepository.save(collectionEntity);

        return userMapper.toDomain(savedEntity);
    }

    @Override
    public User getUser(Integer id) {
        return userMapper.toDomain(userRepository.findById(id).orElse(null));
    }

    @Override
    public Page<User> getUsersByUsername(String username, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCase(username, pageable)
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional
    public User updateUser(UserUpdateRequest updateRequest, Integer id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with id " + id + " not found."));
        if (updateRequest.getUsername() != null)
        {
            if (userRepository.existsByUsernameIgnoreCase(updateRequest.getUsername()) && !userRepository.findByUsernameIgnoreCase(updateRequest.getUsername()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Username is already taken.");
            }
            userEntity.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getEmail() != null)
        {
            if (userRepository.existsByEmailIgnoreCase(updateRequest.getEmail()) && !userRepository.findByEmailIgnoreCase(updateRequest.getEmail()).get().getId().equals(id)) {
                throw new IllegalArgumentException("Email is already taken.");
            }
            userEntity.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPasswordHash() != null)
        {
            userEntity.setPasswordHash(passwordEncoder.encode(updateRequest.getPasswordHash()));
        }
        if (updateRequest.getBio() != null)
        {
            userEntity.setBio(updateRequest.getBio());
        }
        if (updateRequest.getProfileImageUrl() != null)
        {
            userEntity.setProfileImageUrl(updateRequest.getProfileImageUrl());
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
