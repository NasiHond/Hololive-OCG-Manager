package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.repository.CardRepository;
import com.fhict.hololiveocgmanager.repository.CardTypeRepository;
import com.fhict.hololiveocgmanager.repository.ColourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardTypeRepository cardTypeRepository;

    @Mock
    private ColourRepository colourRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void createCardReturnsNullWhenMappingFails() {
        Card card = Card.builder().id(1).build();

        when(cardMapper.toEntity(card)).thenReturn(null);

        assertThat(cardService.createCard(card)).isNull();
        verify(cardRepository, never()).save(any(CardEntity.class));
    }

    @Test
    void createCardResolvesTypeAndColour() {
        Card card = Card.builder().id(1).build();
        CardEntity entity = new CardEntity();
        CardtypeEntity type = new CardtypeEntity();
        type.setName(" Support ");
        ColourEntity colour = new ColourEntity();
        colour.setColour(" Blue ");
        colour.setImageUrl("/blue.png");
        entity.setCardtype(type);
        entity.setCardcolour(colour);

        CardtypeEntity savedType = new CardtypeEntity();
        savedType.setId(5);
        savedType.setName("Support");

        ColourEntity savedColour = new ColourEntity();
        savedColour.setId(7);
        savedColour.setColour("Blue");
        savedColour.setImageUrl("/blue.png");

        CardEntity savedEntity = new CardEntity();
        savedEntity.setId(1);

        when(cardMapper.toEntity(card)).thenReturn(entity);
        when(cardTypeRepository.findByNameIgnoreCase("Support"))
                .thenReturn(Optional.empty());
        when(cardTypeRepository.save(any(CardtypeEntity.class))).thenReturn(savedType);
        when(colourRepository.findByColourIgnoreCase("Blue"))
                .thenReturn(Optional.empty());
        when(colourRepository.save(any(ColourEntity.class))).thenReturn(savedColour);
        when(cardRepository.save(entity)).thenReturn(savedEntity);
        when(cardMapper.toDomain(savedEntity)).thenReturn(Card.builder().id(1).build());

        Card result = cardService.createCard(card);

        assertThat(result.getId()).isEqualTo(1);
        assertThat(entity.getCardtype()).isEqualTo(savedType);
        assertThat(entity.getCardcolour()).isEqualTo(savedColour);
    }

    @Test
    void createCardUpdatesColourImageUrlWhenMissing() {
        Card card = Card.builder().id(1).build();
        CardEntity entity = new CardEntity();
        ColourEntity colour = new ColourEntity();
        colour.setColour("Blue");
        colour.setImageUrl("/blue.png");
        entity.setCardcolour(colour);

        ColourEntity existingColour = new ColourEntity();
        existingColour.setId(1);
        existingColour.setColour("Blue");
        existingColour.setImageUrl(null);

        when(cardMapper.toEntity(card)).thenReturn(entity);
        when(colourRepository.findByColourIgnoreCase("Blue"))
                .thenReturn(Optional.of(existingColour));
        when(colourRepository.save(existingColour)).thenReturn(existingColour);
        when(cardRepository.save(entity)).thenReturn(entity);
        when(cardMapper.toDomain(entity)).thenReturn(Card.builder().id(1).build());

        cardService.createCard(card);

        assertThat(existingColour.getImageUrl()).isEqualTo("/blue.png");
    }

    @Test
    void getCardReturnsNullWhenMissingId() {
        assertThat(cardService.getCard(null)).isNull();
        assertThat(cardService.getCard(Card.builder().build())).isNull();
    }

    @Test
    void getCardReturnsDomain() {
        CardEntity entity = new CardEntity();
        entity.setId(3);
        Card domain = Card.builder().id(3).build();

        when(cardRepository.findById(3)).thenReturn(Optional.of(entity));
        when(cardMapper.toDomain(entity)).thenReturn(domain);

        assertThat(cardService.getCard(Card.builder().id(3).build())).isEqualTo(domain);
    }

    @Test
    void getCardByCardIdRejectsNullAndZero() {
        assertThat(cardService.getCardByCardId(null)).isNull();
        assertThat(cardService.getCardByCardId(0)).isNull();
    }

    @Test
    void getCardByCardIdReturnsDomain() {
        CardEntity entity = new CardEntity();
        entity.setId(4);
        Card domain = Card.builder().id(4).build();

        when(cardRepository.findById(4)).thenReturn(Optional.of(entity));
        when(cardMapper.toDomain(entity)).thenReturn(domain);

        assertThat(cardService.getCardByCardId(4)).isEqualTo(domain);
    }

    @Test
    void getAllCardsReturnsMappedList() {
        CardEntity entity = new CardEntity();
        entity.setId(1);

        when(cardRepository.findAll()).thenReturn(List.of(entity));
        when(cardMapper.toDomain(entity)).thenReturn(Card.builder().id(1).build());

        assertThat(cardService.getAllCards())
                .extracting(Card::getId)
                .containsExactly(1);
    }

    @Test
    void updateCardDelegatesToCreateCard() {
        Card card = Card.builder().id(9).build();
        CardEntity entity = new CardEntity();
        entity.setId(9);

        when(cardMapper.toEntity(card)).thenReturn(entity);
        when(cardRepository.save(entity)).thenReturn(entity);
        when(cardMapper.toDomain(entity)).thenReturn(card);

        assertThat(cardService.updateCard(card)).isEqualTo(card);
    }

    @Test
    void deleteCardIgnoresNulls() {
        cardService.deleteCard(null);
        cardService.deleteCard(Card.builder().build());

        verify(cardRepository, never()).deleteById(any(Integer.class));
    }

    @Test
    void deleteCardDeletesById() {
        cardService.deleteCard(Card.builder().id(4).build());

        verify(cardRepository).deleteById(4);
    }
}

