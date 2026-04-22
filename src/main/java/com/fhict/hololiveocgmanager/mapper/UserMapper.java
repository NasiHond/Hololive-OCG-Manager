package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserEntity toEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(user.getId());
        userEntity.setUsername(user.getUsername());
        userEntity.setEmail(user.getEmail());
        userEntity.setPasswordHash(user.getPasswordHash());
        userEntity.setBio(user.getBio());
        userEntity.setProfileImageUrl(user.getProfileImageURL());
        return userEntity;
    }

    public User toDomain(UserEntity userEntity) {
        if (userEntity == null) {
            return null;
        }
        return User.builder()
                .Id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .passwordHash(userEntity.getPasswordHash())
                .bio(userEntity.getBio())
                .profileImageURL(userEntity.getProfileImageUrl())
                .build();
    }
}
