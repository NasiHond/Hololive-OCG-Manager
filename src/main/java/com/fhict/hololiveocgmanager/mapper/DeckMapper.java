package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Deck;
import com.fhict.hololiveocgmanager.domain.DeckCard;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.DeckCardsEntity;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class DeckMapper {
    public DeckEntity toEntity(Deck deck) {
        DeckEntity.DeckEntityBuilder builder = DeckEntity.builder();
        builder.deckName(deck.getName());
        builder.deckDescription(deck.getDescription());
        builder.visibility(deck.getVisibility());
        if (deck.getOwner() != null) {
            UserEntity ownerEntity = new UserEntity();
            ownerEntity.setId(deck.getOwner().getId());
            builder.creatorId(ownerEntity);
        }
        return builder.build();
    }

    public Deck toDomain(DeckEntity deckEntity) {
        Deck.DeckBuilder builder = Deck.builder();
        builder.id(deckEntity.getId());
        builder.name(deckEntity.getDeckName());
        builder.description(deckEntity.getDeckDescription());
        builder.visibility(deckEntity.getVisibility());
        builder.deckImageUrl(deckEntity.getDeckImageUrl());
        if (deckEntity.getCreatorId() != null) {
            UserEntity ownerEntity = deckEntity.getCreatorId();
            builder.owner(new UserMapper().toDomain(ownerEntity));
        }
        return builder.build();
    }

    public DeckResponse toResponse(Deck deck) {
        DeckResponse.DeckResponseBuilder builder = DeckResponse.builder();
        builder.id(deck.getId());
        builder.title(deck.getName());
        builder.visibility(deck.getVisibility());
        builder.deckImageUrl(null);
        if (deck.getOwner() != null) {
            builder.ownerId(deck.getOwner().getId());
            builder.ownerName(deck.getOwner().getUsername());
        }
        return builder.build();
    }
}