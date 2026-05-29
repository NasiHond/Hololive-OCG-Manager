package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toEntityMapsFields() {
        User user = User.builder()
                .id(1)
                .username("user")
                .email("user@example.com")
                .passwordHash("hash")
                .bio("Bio")
                .profileImageURL("/img.png")
                .build();

        UserEntity entity = mapper.toEntity(user);

        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getUsername()).isEqualTo("user");
        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash");
        assertThat(entity.getBio()).isEqualTo("Bio");
        assertThat(entity.getProfileImageUrl()).isEqualTo("/img.png");
    }

    @Test
    void toDomainReturnsNullWhenEntityIsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomainMapsFields() {
        UserEntity entity = UserEntity.builder()
                .id(2)
                .username("user2")
                .email("user2@example.com")
                .passwordHash("hash")
                .bio("Bio")
                .profileImageUrl("/img2.png")
                .build();

        User user = mapper.toDomain(entity);

        assertThat(user.getId()).isEqualTo(2);
        assertThat(user.getUsername()).isEqualTo("user2");
        assertThat(user.getEmail()).isEqualTo("user2@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hash");
        assertThat(user.getBio()).isEqualTo("Bio");
        assertThat(user.getProfileImageURL()).isEqualTo("/img2.png");
    }
}

