package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Art;
import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.domain.Keyword;
import com.fhict.hololiveocgmanager.domain.Tag;
import com.fhict.hololiveocgmanager.dto.response.ArtResponse;
import com.fhict.hololiveocgmanager.entity.ArtEntity;
import com.fhict.hololiveocgmanager.entity.ArtcostEntity;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardartEntity;
import com.fhict.hololiveocgmanager.entity.CardtagEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.entity.ExtraEntity;
import com.fhict.hololiveocgmanager.entity.KeywordEntity;
import com.fhict.hololiveocgmanager.entity.TagEntity;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CardMapperTest {

    private final CardMapper mapper = new CardMapper();

    @Test
    void toEntityReturnsNullWhenCardIsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntityMapsCoreFields() {
        Card card = Card.builder()
                .id(1)
                .cardID("H-001")
                .cardset("Set-A")
                .cardTypeID(3)
                .cardTypeName("Support")
                .cardColour(" Blue ")
                .batonpass("BP")
                .holomem("Ina")
                .bloomLvl("1")
                .hp(100)
                .rarity("R")
                .imageURL("/img.png")
                .extraEffect("Extra")
                .build();

        CardEntity entity = mapper.toEntity(card);

        assertThat(entity.getId()).isEqualTo(1);
        assertThat(entity.getCardid()).isEqualTo("H-001");
        assertThat(entity.getCardset()).isEqualTo("Set-A");
        assertThat(entity.getBatonpass()).isEqualTo("BP");
        assertThat(entity.getHolomem()).isEqualTo("Ina");
        assertThat(entity.getBloomlvl()).isEqualTo("1");
        assertThat(entity.getHp()).isEqualTo(100);
        assertThat(entity.getRarity()).isEqualTo("R");
        assertThat(entity.getImage()).isEqualTo("/img.png");
        assertThat(entity.getCardtype().getId()).isEqualTo(3);
        assertThat(entity.getCardtype().getName()).isEqualTo("Support");
        assertThat(entity.getCardcolour().getColour()).isEqualTo("Blue");
        assertThat(entity.getExtra().getEffect()).isEqualTo("Extra");
    }

    @Test
    void toDomainMapsRelations() {
        CardtypeEntity cardtype = new CardtypeEntity();
        cardtype.setId(3);
        cardtype.setName("Support");
        ColourEntity colour = new ColourEntity();
        colour.setId(4);
        colour.setColour("Blue");
        ExtraEntity extra = new ExtraEntity();
        extra.setId(5);
        extra.setEffect("Extra");

        KeywordEntity keyword = new KeywordEntity();
        keyword.setId(1);
        keyword.setType("Skill");
        keyword.setName("Bloom");
        keyword.setEffect("Grow");

        TagEntity tag = new TagEntity();
        tag.setId(7);
        tag.setName("Idol");
        CardtagEntity cardtag = new CardtagEntity();
        cardtag.setTagid(tag);

        ArtcostEntity cost1 = new ArtcostEntity();
        cost1.setId(2);
        cost1.setAmount(1);
        cost1.setColour(colour);
        ArtcostEntity cost2 = new ArtcostEntity();
        cost2.setId(1);
        cost2.setAmount(2);
        cost2.setColour(colour);

        ArtEntity artEntity = ArtEntity.builder()
                .id(9)
                .name("Strike")
                .effect("Deal damage")
                .damage(30)
                .critColour(colour)
                .artcosts(Set.of(cost1, cost2))
                .build();

        CardartEntity cardart = CardartEntity.builder()
                .artid(artEntity)
                .build();

        CardEntity entity = new CardEntity();
        entity.setId(1);
        entity.setCardid("H-001");
        entity.setCardset("Set-A");
        entity.setBatonpass("BP");
        entity.setHolomem("Ina");
        entity.setBloomlvl("1");
        entity.setHp(100);
        entity.setRarity("R");
        entity.setImage("/img.png");
        entity.setCardtype(cardtype);
        entity.setCardcolour(colour);
        entity.setExtra(extra);
        entity.setKeywords(Set.of(keyword));
        entity.setCardtags(Set.of(cardtag));
        entity.setCardarts(Set.of(cardart));

        Card card = mapper.toDomain(entity);

        assertThat(card.getCardTypeID()).isEqualTo(3);
        assertThat(card.getCardTypeName()).isEqualTo("Support");
        assertThat(card.getCardColour()).isEqualTo("Blue");
        assertThat(card.getExtraID()).isEqualTo(5);
        assertThat(card.getExtraEffect()).isEqualTo("Extra");
        assertThat(card.getKeywords()).extracting(Keyword::getName).containsExactly("Bloom");
        assertThat(card.getTags()).extracting(Tag::getName).containsExactly("Idol");
        assertThat(card.getArts()).extracting(Art::getName).containsExactly("Strike");
        assertThat(card.getArts().getFirst().getCosts()).extracting(ArtcostEntity::getId)
                .containsExactly(1, 2);
    }

    @Test
    void mapArtsToResponseReturnsSortedCosts() {
        ColourEntity colour = new ColourEntity();
        colour.setColour("Blue");
        colour.setImageUrl("/blue.png");

        ArtcostEntity cost1 = new ArtcostEntity();
        cost1.setId(2);
        cost1.setAmount(1);
        cost1.setColour(colour);
        ArtcostEntity cost2 = new ArtcostEntity();
        cost2.setId(1);
        cost2.setAmount(2);
        cost2.setColour(colour);

        ArtEntity artEntity = ArtEntity.builder()
                .id(9)
                .name("Strike")
                .effect("Deal damage")
                .damage(30)
                .critColour(colour)
                .artcosts(Set.of(cost1, cost2))
                .build();
        CardartEntity cardart = CardartEntity.builder().artid(artEntity).build();

        CardEntity cardEntity = new CardEntity();
        cardEntity.setCardarts(Set.of(cardart));

        List<ArtResponse> responses = mapper.mapArtsToResponse(cardEntity);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getCosts()).extracting("id").containsExactly(1, 2);
        assertThat(responses.getFirst().getCosts()).extracting("colourName").containsExactly("Blue", "Blue");
    }

    @Test
    void toDomainReturnsEmptyListsWhenRelationsMissing() {
        CardEntity entity = new CardEntity();
        entity.setId(1);

        Card card = mapper.toDomain(entity);

        assertThat(card.getArts()).isEmpty();
        assertThat(card.getKeywords()).isEmpty();
        assertThat(card.getTags()).isEmpty();
    }
}

