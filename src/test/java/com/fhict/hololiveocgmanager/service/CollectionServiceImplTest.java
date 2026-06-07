package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardtagEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.entity.CollectionCardsEntity;
import com.fhict.hololiveocgmanager.entity.CollectionEntity;
import com.fhict.hololiveocgmanager.entity.KeywordEntity;
import com.fhict.hololiveocgmanager.entity.TagEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.BadRequestException;
import com.fhict.hololiveocgmanager.exception.ForbiddenException;
import com.fhict.hololiveocgmanager.exception.NotFoundException;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.repository.CollectionCardsRepository;
import com.fhict.hololiveocgmanager.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionServiceImplTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CollectionCardsRepository collectionCardsRepository;

    @Mock
    private CardService cardService;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CollectionServiceImpl collectionService;

    @Test
    void getCollectionByUserIdRejectsMissingCollection() {
        when(collectionRepository.findByOwnerId_Id(10)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.getCollectionByUserId(10, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Collection not found for user");
    }

    @Test
    void getCollectionByUserIdReturnsPageResponse() {
        CollectionEntity collection = CollectionEntity.builder()
                .id(1)
                .ownerId(UserEntity.builder().id(3).build())
                .visibility(Visibility.PUBLIC)
                .build();
        CollectionCardsEntity cardRow = CollectionCardsEntity.builder()
                .id(5)
                .collectionId(collection)
                .cardId(sampleCardEntity())
                .cardCount(2)
                .build();

        when(collectionRepository.findByOwnerId_Id(3)).thenReturn(Optional.of(collection));
        when(collectionCardsRepository.findByCollectionId_Id(eq(1), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(cardRow), PageRequest.of(0, 20), 1));
        when(collectionCardsRepository.sumCardCountByCollectionId_Id(1)).thenReturn(5L);

        CollectionResponse response = collectionService.getCollectionByUserId(3, null);

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getOwner().getId()).isEqualTo(3);
        assertThat(response.getVisibility()).isEqualTo(Visibility.PUBLIC);
        assertThat(response.getTotalCards()).isEqualTo(1);
        assertThat(response.getTotalCount()).isEqualTo(5);
    }

    @Test
    void updateCollectionCardRejectsMissingCardId() {
        assertThatThrownBy(() -> collectionService.updateCollectionCardByUserId(1, 1, null, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("cardId is required");
    }

    @Test
    void updateCollectionCardRejectsNegativeAmount() {
        assertThatThrownBy(() -> collectionService.updateCollectionCardByUserId(1, 1, 2, -1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("amount must be non-negative");
    }

    @Test
    void updateCollectionCardRejectsMissingCollection() {
        when(collectionRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.updateCollectionCardByUserId(1, 1, 2, 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Collection not found");
    }

    @Test
    void updateCollectionCardRejectsUnauthorizedUser() {
        CollectionEntity collection = CollectionEntity.builder()
                .id(1)
                .ownerId(UserEntity.builder().id(2).build())
                .build();

        when(collectionRepository.findById(1)).thenReturn(Optional.of(collection));

        assertThatThrownBy(() -> collectionService.updateCollectionCardByUserId(1, 1, 2, 1))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("You do not have permission to modify this collection");
    }

    @Test
    void updateCollectionCardDeletesWhenAmountZero() {
        CollectionEntity collection = CollectionEntity.builder()
                .id(1)
                .ownerId(UserEntity.builder().id(1).build())
                .build();
        CardEntity card = sampleCardEntity();
        CollectionCardsEntity existing = CollectionCardsEntity.builder()
                .id(9)
                .collectionId(collection)
                .cardId(card)
                .cardCount(2)
                .build();

        when(collectionRepository.findById(1)).thenReturn(Optional.of(collection));
        when(cardService.getCardByCardId(2)).thenReturn(Card.builder().cardID("H-001").holomem("Ina").build());
        when(cardMapper.toEntity(any(Card.class))).thenReturn(card);
        when(collectionCardsRepository.findByCollectionId_IdAndCardId_Id(1, 2)).thenReturn(Optional.of(existing));

        CollectionCardResponse response = collectionService.updateCollectionCardByUserId(1, 1, 2, 0);

        verify(collectionCardsRepository).delete(existing);
        assertThat(response.getCardCount()).isEqualTo(0);
        assertThat(response.getCardId()).isEqualTo("H-001");
    }

    @Test
    void updateCollectionCardUpdatesExistingRow() {
        CollectionEntity collection = CollectionEntity.builder()
                .id(1)
                .ownerId(UserEntity.builder().id(1).build())
                .build();
        CardEntity card = sampleCardEntity();
        CollectionCardsEntity existing = CollectionCardsEntity.builder()
                .id(9)
                .collectionId(collection)
                .cardId(card)
                .cardCount(2)
                .build();

        when(collectionRepository.findById(1)).thenReturn(Optional.of(collection));
        when(cardService.getCardByCardId(2)).thenReturn(Card.builder().cardID("H-001").holomem("Ina").build());
        when(cardMapper.toEntity(any(Card.class))).thenReturn(card);
        when(collectionCardsRepository.findByCollectionId_IdAndCardId_Id(1, 2)).thenReturn(Optional.of(existing));
        when(collectionCardsRepository.save(existing)).thenReturn(existing);

        CollectionCardResponse response = collectionService.updateCollectionCardByUserId(1, 1, 2, 4);

        assertThat(existing.getCardCount()).isEqualTo(4);
        assertThat(response.getCardCount()).isEqualTo(4);
    }

    @Test
    void updateCollectionCardCreatesNewRow() {
        CollectionEntity collection = CollectionEntity.builder()
                .id(1)
                .ownerId(UserEntity.builder().id(1).build())
                .build();
        CardEntity card = sampleCardEntity();

        when(collectionRepository.findById(1)).thenReturn(Optional.of(collection));
        when(cardService.getCardByCardId(2)).thenReturn(Card.builder().cardID("H-001").holomem("Ina").build());
        when(cardMapper.toEntity(any(Card.class))).thenReturn(card);
        when(collectionCardsRepository.findByCollectionId_IdAndCardId_Id(1, 2)).thenReturn(Optional.empty());
        when(collectionCardsRepository.save(any(CollectionCardsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CollectionCardResponse response = collectionService.updateCollectionCardByUserId(1, 1, 2, 2);

        assertThat(response.getCardCount()).isEqualTo(2);
        assertThat(response.getCardId()).isEqualTo("H-001");
    }

    @Test
    void updateCollectionCardReturnsMinimalResponseWhenAmountZeroAndMissingRow() {
        CollectionEntity collection = CollectionEntity.builder()
                .id(1)
                .ownerId(UserEntity.builder().id(1).build())
                .build();
        CardEntity card = sampleCardEntity();

        when(collectionRepository.findById(1)).thenReturn(Optional.of(collection));
        when(cardService.getCardByCardId(2)).thenReturn(Card.builder().cardID("H-001").holomem("Ina").build());
        when(cardMapper.toEntity(any(Card.class))).thenReturn(card);
        when(collectionCardsRepository.findByCollectionId_IdAndCardId_Id(1, 2)).thenReturn(Optional.empty());

        CollectionCardResponse response = collectionService.updateCollectionCardByUserId(1, 1, 2, 0);

        verify(collectionCardsRepository, never()).save(any(CollectionCardsEntity.class));
        assertThat(response.getCardCount()).isEqualTo(0);
        assertThat(response.getCardId()).isEqualTo("H-001");
    }

    private CardEntity sampleCardEntity() {
        CardtypeEntity cardtype = new CardtypeEntity();
        cardtype.setName("Support");
        ColourEntity colour = new ColourEntity();
        colour.setColour("Blue");

        KeywordEntity keyword = new KeywordEntity();
        keyword.setId(1);
        keyword.setName("Bloom");
        keyword.setType("Skill");
        keyword.setEffect("Grow");

        TagEntity tag = new TagEntity();
        tag.setId(2);
        tag.setName("Idol");

        CardtagEntity cardtag = new CardtagEntity();
        cardtag.setTagid(tag);

        CardEntity card = new CardEntity();
        card.setId(11);
        card.setCardid("H-001");
        card.setHolomem("Ina");
        card.setCardset("Set-A");
        card.setBatonpass("BP");
        card.setBloomlvl("1");
        card.setHp(100);
        card.setRarity("R");
        card.setCardtype(cardtype);
        card.setCardcolour(colour);
        card.setKeywords(Set.of(keyword));
        card.setCardtags(Set.of(cardtag));

        return card;
    }
}
