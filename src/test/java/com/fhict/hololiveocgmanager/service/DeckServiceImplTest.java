package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Deck;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.request.CreateDeckRequest;
import com.fhict.hololiveocgmanager.dto.request.DeckCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.request.UpdateDeckRequest;
import com.fhict.hololiveocgmanager.dto.response.DeckCardResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckPageResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.DeckCardsEntity;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.BadRequestException;
import com.fhict.hololiveocgmanager.exception.NotFoundException;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.mapper.DeckMapper;
import com.fhict.hololiveocgmanager.repository.CardRepository;
import com.fhict.hololiveocgmanager.repository.DeckCardsRepository;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeckServiceImplTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private DeckMapper deckMapper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private DeckCardsRepository deckCardsRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeckServiceImpl deckService;

//    @Test
//    void createDeckRejectsMissingTitle() {
//        CreateDeckRequest request = new CreateDeckRequest();
//        UserEntity user = userRepository.findAll().getFirst();
//        request.setVisibility(Visibility.PUBLIC);
//
//        assertThatThrownBy(() -> deckService.createDeck(request, user))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessage("Deck must have a title.");
//    }

//    @Test
//    void createDeckReturnsResponse() {
//        CreateDeckRequest request = new CreateDeckRequest();
//        request.setTitle("Deck");
//        request.setVisibility(Visibility.PUBLIC);
//        request.setDescription("Desc");
//
//        DeckEntity entity = DeckEntity.builder()
//                .id(1)
//                .creatorId(UserEntity.builder().id(1).build())
//                .deckName("Deck")
//                .visibility(Visibility.PUBLIC)
//                .build();
//
//        Deck domain = Deck.builder().id(1).name("Deck").visibility(Visibility.PUBLIC).build();
//        DeckResponse response = DeckResponse.builder().id(1).title("Deck").visibility(Visibility.PUBLIC).build();
//
//        when(deckMapper.toEntity(any(Deck.class))).thenReturn(entity);
//        when(deckRepository.save(entity)).thenReturn(entity);
//        when(deckMapper.toDomain(entity)).thenReturn(domain);
//        when(deckMapper.toResponse(domain)).thenReturn(response);
//
//        UserEntity user = userRepository.findAll().getFirst();
//
//        DeckResponse result = deckService.createDeck(request, user);
//
//        assertThat(result.getId()).isEqualTo(1);
//        assertThat(result.getTitle()).isEqualTo("Deck");
//    }

    @Test
    void getDeckPageRejectsMissingDeck() {
        when(deckRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.getDeckPage(1))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Deck not found");
    }

    @Test
    void getDeckPageReturnsCards() {
        DeckEntity deckEntity = DeckEntity.builder().id(1).build();
        DeckCardsEntity deckCard = DeckCardsEntity.builder()
                .cardId(CardEntity.builder().id(2).build())
                .cardCount(3)
                .build();

        when(deckRepository.findById(1)).thenReturn(Optional.of(deckEntity));
        when(deckCardsRepository.findAllByDeckId(deckEntity)).thenReturn(Optional.of(List.of(deckCard)));
        when(cardRepository.findById(2)).thenReturn(Optional.of(CardEntity.builder().id(2).build()));
        when(cardMapper.toDeckCardResponse(any(CardEntity.class), eq(1), eq(3)))
                .thenReturn(DeckCardResponse.builder().deckId(1).count(3).build());
        when(deckMapper.toDomain(deckEntity)).thenReturn(Deck.builder().id(1).name("Deck").build());
        when(deckMapper.toResponse(any(Deck.class))).thenReturn(DeckResponse.builder().id(1).title("Deck").build());

        DeckPageResponse response = deckService.getDeckPage(1);

        assertThat(response.getDeck().getId()).isEqualTo(1);
        assertThat(response.getCards()).hasSize(1);
        assertThat(response.getCards().getFirst().getCount()).isEqualTo(3);
    }

    @Test
    void getDeckCardsByCardIdAndUserIdReturnsResponses() {
        DeckEntity deck = DeckEntity.builder().id(1).build();
        DeckCardsEntity deckCards = DeckCardsEntity.builder().id(2).build();

        when(deckRepository.findAllByCreatorId_Id(5)).thenReturn(List.of(deck));
        when(deckCardsRepository.findAllByCardId_IdAndDeckId(3, deck)).thenReturn(Optional.of(deckCards));
        when(cardMapper.deckCardToDomain(deckCards)).thenReturn(null);
        when(cardMapper.toDeckCardResponse(any())).thenReturn(DeckCardResponse.builder().deckId(1).count(1).build());

        List<DeckCardResponse> responses = deckService.getDeckCardsByCardIdAndUserId(3, 5);

        assertThat(responses).hasSize(1);
    }

    @Test
    void getPublicDecksByUserReturnsPage() {
        DeckEntity deckEntity = DeckEntity.builder().id(1).build();
        Deck domain = Deck.builder().id(1).build();
        DeckResponse response = DeckResponse.builder().id(1).build();

        when(deckRepository.findAllByCreatorId_IdAndVisibility(eq(5), eq(Visibility.PUBLIC), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(deckEntity), PageRequest.of(0, 20), 1));
        when(deckMapper.toDomain(deckEntity)).thenReturn(domain);
        when(deckMapper.toResponse(domain)).thenReturn(response);

        assertThat(deckService.getPublicDecksByUser(5, PageRequest.of(0, 20)).getContent()).containsExactly(response);
    }

    @Test
    void getVisibleDecksReturnsPublicWhenAnonymous() {
        DeckEntity deckEntity = DeckEntity.builder().id(1).build();
        Deck domain = Deck.builder().id(1).build();
        DeckResponse response = DeckResponse.builder().id(1).build();

        when(deckRepository.findAllByVisibility(eq(Visibility.PUBLIC), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(deckEntity), PageRequest.of(0, 20), 1));
        when(deckMapper.toDomain(deckEntity)).thenReturn(domain);
        when(deckMapper.toResponse(domain)).thenReturn(response);

        assertThat(deckService.getVisibleDecks(null, PageRequest.of(0, 20)).getContent()).containsExactly(response);
    }

    @Test
    void updateDeckCardRejectsMissingFields() {
        assertThatThrownBy(() -> deckService.updateDeckCard(1, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Update request body is required");

        DeckCardUpdateRequest missingCardId = DeckCardUpdateRequest.builder().count(1).build();
        assertThatThrownBy(() -> deckService.updateDeckCard(1, missingCardId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Card id is required");

        DeckCardUpdateRequest missingCount = DeckCardUpdateRequest.builder().cardId(1).build();
        assertThatThrownBy(() -> deckService.updateDeckCard(1, missingCount))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Card count is required");

        DeckCardUpdateRequest negativeCount = DeckCardUpdateRequest.builder().cardId(1).count(-1).build();
        assertThatThrownBy(() -> deckService.updateDeckCard(1, negativeCount))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Card count cannot be negative");
    }

//    @Test
//    void updateDeckCardDeletesWhenCountZero() {
//        UserEntity user = UserEntity.builder().id(1).build();
//        DeckEntity deckEntity = DeckEntity.builder().id(1).creatorId(user).build();
//        CardEntity cardEntity = CardEntity.builder().id(2).build();
//        DeckCardsEntity deckCards = DeckCardsEntity.builder().id(3).deckId(deckEntity).cardId(cardEntity).cardCount(2).build();
//
//        DeckCardUpdateRequest request = DeckCardUpdateRequest.builder().cardId(2).count(0).build();
//
//        when(deckRepository.findById(1)).thenReturn(Optional.of(deckEntity));
//        when(cardRepository.findById(2)).thenReturn(Optional.of(cardEntity));
//        when(deckCardsRepository.findByDeckId_IdAndCardId_Id(1, 2)).thenReturn(Optional.of(deckCards));
//        when(cardMapper.toDeckCardResponse(cardEntity, 1, 0))
//                .thenReturn(DeckCardResponse.builder().deckId(1).count(0).build());
//
//        DeckCardResponse response = deckService.updateDeckCard(1, request);
//
//        verify(deckCardsRepository).delete(deckCards);
//        assertThat(response.getCount()).isZero();
//    }

    @Test
    void updateDeckByUserIdUpdatesFields() {
        DeckEntity deckEntity = DeckEntity.builder().id(1).deckName("Old").build();
        DeckResponse response = DeckResponse.builder().id(1).title("New").build();

        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setTitle("New");
        request.setVisibility(Visibility.PUBLIC);

        when(deckRepository.findById(1)).thenReturn(Optional.of(deckEntity));
        when(deckRepository.save(deckEntity)).thenReturn(deckEntity);
        when(deckMapper.toDomain(deckEntity)).thenReturn(Deck.builder().id(1).name("New").build());
        when(deckMapper.toResponse(any(Deck.class))).thenReturn(response);

        DeckResponse updated = deckService.updateDeckByUserId(1, request);

        assertThat(updated.getTitle()).isEqualTo("New");
        assertThat(deckEntity.getDeckName()).isEqualTo("New");
        assertThat(deckEntity.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    void updateDeckByUserIdRejectsMissingDeck() {
        when(deckRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deckService.updateDeckByUserId(1, new UpdateDeckRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Deck not found");
    }
}
