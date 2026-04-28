package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Art;
import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.dto.response.ArtcostResponse;
import com.fhict.hololiveocgmanager.dto.response.ArtResponse;
import com.fhict.hololiveocgmanager.entity.ArtEntity;
import com.fhict.hololiveocgmanager.entity.ArtcostEntity;
import com.fhict.hololiveocgmanager.entity.CardartEntity;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.entity.ExtraEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class CardMapper {
    public CardEntity toEntity(Card card)
    {
        if (card == null)
        {
            return null;
        }

        CardEntity.CardEntityBuilder builder = CardEntity.builder()
                .id(card.getID())
                .cardid(card.getCardID())
                .cardset(card.getCardset())
                .batonpass(card.getBatonpass())
                .holomem(card.getHolomem())
                .bloomlvl(card.getBloomLvl())
                .hp(card.getHp())
                .rarity(card.getRarity())
                .image(card.getImageURL());

        if (card.getCardTypeID() != null)
        {
            CardtypeEntity cardtype = new CardtypeEntity();
            cardtype.setId(card.getCardTypeID());
            cardtype.setName(card.getCardTypeName());
            builder.cardtype(cardtype);
        } else if (card.getCardTypeName() != null && !card.getCardTypeName().isBlank()) {
            CardtypeEntity cardtype = new CardtypeEntity();
            cardtype.setName(card.getCardTypeName().trim());
            builder.cardtype(cardtype);
        }

        if (card.getCardColour() != null)
        {
            ColourEntity colour = new ColourEntity();
            colour.setColour(card.getCardColour().trim());
            builder.cardcolour(colour);
        }

        if (card.getExtraID() != null)
        {
            ExtraEntity entity = new ExtraEntity();
            entity.setId(card.getCardTypeID());
            entity.setEffect(card.getExtraEffect());
            builder.extra(entity);
        } else if (card.getExtraEffect() != null && !card.getExtraEffect().isBlank()) {
            ExtraEntity entity = new ExtraEntity();
            entity.setEffect(card.getExtraEffect());
            builder.extra(entity);
        }

        return builder.build();
    }

    public Card toDomain(CardEntity cardEntity) {
        if (cardEntity == null) {
            return null;
        }

        Card.CardBuilder builder = Card.builder()
                .ID(cardEntity.getId())
                .cardID(cardEntity.getCardid())
                .cardset(cardEntity.getCardset())
                .batonpass(cardEntity.getBatonpass())
                .holomem(cardEntity.getHolomem())
                .bloomLvl(cardEntity.getBloomlvl())
                .hp(cardEntity.getHp())
                .rarity(cardEntity.getRarity())
                .extraID(cardEntity.getExtra() != null ? cardEntity.getExtra().getId() : null)
                .extraEffect(cardEntity.getExtra() != null ? cardEntity.getExtra().getEffect() : null)
                .arts(mapArts(cardEntity))
                .imageURL(cardEntity.getImage());

        if (cardEntity.getCardtype() != null) {
            builder.cardTypeID(cardEntity.getCardtype().getId());
            builder.cardTypeName(cardEntity.getCardtype().getName());
        }

        if (cardEntity.getCardcolour() != null) {
            builder.cardColour(cardEntity.getCardcolour().getColour());
        }

        return builder.build();
    }

    private List<Art> mapArts(CardEntity cardEntity) {
        if (cardEntity.getCardarts() == null || cardEntity.getCardarts().isEmpty()) {
            return List.of();
        }

        return cardEntity.getCardarts().stream()
                .map(CardartEntity::getArtid)
                .filter(Objects::nonNull)
                .map(this::mapArt)
                .toList();
    }

    private Art mapArt(ArtEntity artEntity) {
        return Art.builder()
                .ID(artEntity.getId())
                .name(artEntity.getName())
                .effect(artEntity.getEffect())
                .damage(artEntity.getDamage())
                .critColourName(artEntity.getCritColour() != null ? artEntity.getCritColour().getColour() : null)
                .costs(mapArtCosts(artEntity))
                .build();
    }

    private List<ArtcostEntity> mapArtCosts(ArtEntity artEntity) {
        if (artEntity.getArtcosts() == null || artEntity.getArtcosts().isEmpty()) {
            return List.of();
        }

        return artEntity.getArtcosts().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ArtcostEntity::getId, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    public List<ArtResponse> mapArtsToResponse(CardEntity cardEntity) {
        if (cardEntity.getCardarts() == null || cardEntity.getCardarts().isEmpty()) {
            return List.of();
        }

        return cardEntity.getCardarts().stream()
                .map(CardartEntity::getArtid)
                .filter(Objects::nonNull)
                .map(this::mapArtToResponse)
                .toList();
    }

    private ArtResponse mapArtToResponse(ArtEntity artEntity) {
        return ArtResponse.builder()
                .id(artEntity.getId())
                .name(artEntity.getName())
                .effect(artEntity.getEffect())
                .damage(artEntity.getDamage())
                .critColourName(artEntity.getCritColour() != null ? artEntity.getCritColour().getColour() : null)
                .costs(mapArtCostsToResponse(artEntity))
                .build();
    }

    private List<ArtcostResponse> mapArtCostsToResponse(ArtEntity artEntity) {
        if (artEntity.getArtcosts() == null || artEntity.getArtcosts().isEmpty()) {
            return List.of();
        }

        return artEntity.getArtcosts().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ArtcostEntity::getId, Comparator.nullsLast(Integer::compareTo)))
                .map(cost -> ArtcostResponse.builder()
                        .id(cost.getId())
                        .amount(cost.getAmount())
                        .colourName(cost.getColour() != null ? cost.getColour().getColour() : null)
                        .colourImageUrl(cost.getColour() != null ? cost.getColour().getImageUrl() : null)
                        .build())
                .toList();
    }
}
