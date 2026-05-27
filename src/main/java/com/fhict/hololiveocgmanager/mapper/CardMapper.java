package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.*;
import com.fhict.hololiveocgmanager.dto.response.ArtResponse;
import com.fhict.hololiveocgmanager.dto.response.ArtcostResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckCardResponse;
import com.fhict.hololiveocgmanager.dto.response.KeywordResponse;
import com.fhict.hololiveocgmanager.dto.response.TagResponse;
import com.fhict.hololiveocgmanager.entity.*;
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
                .id(card.getId())
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
                .id(cardEntity.getId())
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
                .keywords(mapKeywords(cardEntity))
                .tags(mapTags(cardEntity))
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

    public DeckCard deckCardToDomain(DeckCardsEntity deckCardsEntity)
    {
        if (deckCardsEntity == null) {
            return null;
        }

        CardEntity cardEntity = deckCardsEntity.getCardId();
        Integer deckId = deckCardsEntity.getDeckId() != null ? deckCardsEntity.getDeckId().getId() : null;
        Integer count = deckCardsEntity.getCardCount();

        DeckCard.DeckCardBuilder builder = DeckCard.builder()
                .id(deckCardsEntity.getId())
                .deckId(deckId)
                .count(count != null ? count : 0);

        if (cardEntity == null) {
            return builder.build();
        }

        builder
                .cardID(cardEntity.getCardid())
                .cardset(cardEntity.getCardset())
                .batonpass(cardEntity.getBatonpass())
                .holomem(cardEntity.getHolomem())
                .bloomLvl(cardEntity.getBloomlvl())
                .hp(cardEntity.getHp())
                .rarity(cardEntity.getRarity())
                .imageURL(cardEntity.getImage())
                .keywords(mapKeywords(cardEntity))
                .extraID(cardEntity.getExtra() != null ? cardEntity.getExtra().getId() : null)
                .extraEffect(cardEntity.getExtra() != null ? cardEntity.getExtra().getEffect() : null)
                .arts(mapArts(cardEntity))
                .tags(mapTags(cardEntity));

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
                .id(artEntity.getId())
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

    private List<ArtResponse> mapArtsToResponse(DeckCard deckCard)
    {
        if (deckCard.getArts() == null || deckCard.getArts().isEmpty()) {
            return List.of();
        }

        return deckCard.getArts().stream()
                .map(art -> ArtResponse.builder()
                        .id(art.getId())
                        .name(art.getName())
                        .effect(art.getEffect())
                        .damage(art.getDamage())
                        .critColourName(art.getCritColourName())
                        .costs(mapArtCostsToResponse(art.getCosts()))
                        .build())
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

    private List<ArtcostResponse> mapArtCostsToResponse(List<ArtcostEntity> costs) {
        if (costs == null || costs.isEmpty()) {
            return List.of();
        }
        return costs.stream()
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

    private List<Keyword> mapKeywords(CardEntity cardEntity) {
        if (cardEntity.getKeywords() == null || cardEntity.getKeywords().isEmpty()) {
            return List.of();
        }

        return cardEntity.getKeywords().stream()
                .filter(Objects::nonNull)
                .map(keywordEntity -> Keyword.builder()
                        .ID(keywordEntity.getId())
                        .type(keywordEntity.getType())
                        .name(keywordEntity.getName())
                        .effect(keywordEntity.getEffect())
                        .build())
                .toList();
    }

    private List<Tag> mapTags(CardEntity cardEntity) {
        if (cardEntity.getCardtags() == null || cardEntity.getCardtags().isEmpty()) {
            return List.of();
        }

        return cardEntity.getCardtags().stream()
                .map(CardtagEntity::getTagid)
                .filter(Objects::nonNull)
                .map(tagEntity -> Tag.builder()
                        .id(tagEntity.getId())
                        .name(tagEntity.getName())
                        .build())
                .toList();
    }

    public DeckCardResponse toDeckCardResponse(DeckCard deckCard) {
        DeckCardResponse.DeckCardResponseBuilder builder = DeckCardResponse.builder()
                .id(deckCard.getId())
                .deckId(deckCard.getDeckId())
                .count(deckCard.getCount())
                .cardID(deckCard.getCardID())
                .cardset(deckCard.getCardset())
                .batonpass(deckCard.getBatonpass())
                .holomem(deckCard.getHolomem())
                .bloomLvl(deckCard.getBloomLvl())
                .hp(deckCard.getHp())
                .rarity(deckCard.getRarity())
                .imageURL(deckCard.getImageURL())
                .keywords(mapKeywordsToResponse(deckCard))
                .extraID(deckCard.getExtraID())
                .extraEffect(deckCard.getExtraEffect())
                .arts(mapArtsToResponse(deckCard))
                .tags(mapTagsToResponse(deckCard));

        return builder.build();
    }

    public DeckCardResponse toDeckCardResponse(CardEntity cardEntity, Integer deckId, Integer count) {
        DeckCardResponse.DeckCardResponseBuilder builder = DeckCardResponse.builder()
                .deckId(deckId)
                .count(count != null ? count : 0);

        if (cardEntity == null) {
            return builder.build();
        }

        return builder
                .id(cardEntity.getId())
                .cardID(cardEntity.getCardid())
                .cardset(cardEntity.getCardset())
                .cardTypeName(cardEntity.getCardtype() != null ? cardEntity.getCardtype().getName() : null)
                .cardColour(cardEntity.getCardcolour() != null ? cardEntity.getCardcolour().getColour() : null)
                .batonpass(cardEntity.getBatonpass())
                .holomem(cardEntity.getHolomem())
                .bloomLvl(cardEntity.getBloomlvl())
                .hp(cardEntity.getHp())
                .rarity(cardEntity.getRarity())
                .imageURL(cardEntity.getImage())
                .keywords(mapKeywordsToResponse(cardEntity))
                .extraID(cardEntity.getExtra() != null ? cardEntity.getExtra().getId() : null)
                .extraEffect(cardEntity.getExtra() != null ? cardEntity.getExtra().getEffect() : null)
                .arts(mapArtsToResponse(cardEntity))
                .tags(mapTagsToResponse(cardEntity))
                .build();
    }

    private List<KeywordResponse> mapKeywordsToResponse(DeckCard deckCard)
    {
        if(deckCard.getKeywords() == null) {
            return List.of();
        }

        return deckCard.getKeywords().stream()
                .filter(Objects::nonNull)
                .map(keyword -> KeywordResponse.builder()
                        .id(keyword.getID())
                        .type(keyword.getType())
                        .name(keyword.getName())
                        .effect(keyword.getEffect())
                        .build())
                .toList();
    }

    private List<KeywordResponse> mapKeywordsToResponse(CardEntity cardEntity) {
        if (cardEntity.getKeywords() == null || cardEntity.getKeywords().isEmpty()) {
            return List.of();
        }

        return cardEntity.getKeywords().stream()
                .filter(Objects::nonNull)
                .map(keyword -> KeywordResponse.builder()
                        .id(keyword.getId())
                        .type(keyword.getType())
                        .name(keyword.getName())
                        .effect(keyword.getEffect())
                        .build())
                .toList();
    }

    private List<TagResponse> mapTagsToResponse(DeckCard deckCard)
    {
        if(deckCard.getTags() == null) {
            return List.of();
        }

        return deckCard.getTags().stream()
                .filter(Objects::nonNull)
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .toList();
    }

    private List<TagResponse> mapTagsToResponse(CardEntity cardEntity) {
        if (cardEntity.getCardtags() == null || cardEntity.getCardtags().isEmpty()) {
            return List.of();
        }

        return cardEntity.getCardtags().stream()
                .map(CardtagEntity::getTagid)
                .filter(Objects::nonNull)
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .build())
                .toList();
    }
}