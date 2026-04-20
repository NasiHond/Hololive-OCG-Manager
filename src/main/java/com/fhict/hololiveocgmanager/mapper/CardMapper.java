package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.entity.ExtraEntity;
import org.springframework.stereotype.Component;

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
}
