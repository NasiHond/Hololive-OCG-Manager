package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Deck;
import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeckMapperTest {

    private final DeckMapper mapper = new DeckMapper();

    @Test
    void toEntityMapsOwnerWhenPresent() {
        Deck deck = Deck.builder()
                .name("Deck")
                .description("Desc")
                .visibility(Visibility.PUBLIC)
                .owner(User.builder().id(3).build())
                .build();

        DeckEntity entity = mapper.toEntity(deck);

        assertThat(entity.getDeckName()).isEqualTo("Deck");
        assertThat(entity.getDeckDescription()).isEqualTo("Desc");
        assertThat(entity.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(entity.getCreatorId().getId()).isEqualTo(3);
    }

    @Test
    void toDomainMapsFields() {
        DeckEntity entity = DeckEntity.builder()
                .id(2)
                .deckName("Deck")
                .deckDescription("Desc")
                .visibility(Visibility.PRIVATE)
                .deckImageUrl("/img.png")
                .creatorId(UserEntity.builder().id(5).username("user").email("u@example.com").passwordHash("hash").build())
                .build();

        Deck deck = mapper.toDomain(entity);

        assertThat(deck.getId()).isEqualTo(2);
        assertThat(deck.getName()).isEqualTo("Deck");
        assertThat(deck.getDescription()).isEqualTo("Desc");
        assertThat(deck.getVisibility()).isEqualTo(Visibility.PRIVATE);
        assertThat(deck.getDeckImageUrl()).isEqualTo("/img.png");
        assertThat(deck.getOwner().getId()).isEqualTo(5);
        assertThat(deck.getOwner().getUsername()).isEqualTo("user");
    }

    @Test
    void toResponseMapsFields() {
        Deck deck = Deck.builder()
                .id(3)
                .name("Deck")
                .visibility(Visibility.PUBLIC)
                .owner(User.builder().id(7).username("owner").build())
                .build();

        DeckResponse response = mapper.toResponse(deck);

        assertThat(response.getId()).isEqualTo(3);
        assertThat(response.getTitle()).isEqualTo("Deck");
        assertThat(response.getOwnerId()).isEqualTo(7);
        assertThat(response.getOwnerName()).isEqualTo("owner");
        assertThat(response.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(response.getDeckImageUrl()).isNull();
    }
}

