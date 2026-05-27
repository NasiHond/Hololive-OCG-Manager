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
import com.fhict.hololiveocgmanager.exception.BadRequestException;
import com.fhict.hololiveocgmanager.exception.NotFoundException;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.mapper.DeckMapper;
import com.fhict.hololiveocgmanager.repository.CardRepository;
import com.fhict.hololiveocgmanager.repository.DeckCardsRepository;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeckServiceImpl implements DeckService
{
    private final DeckRepository deckRepository;
    private final DeckMapper deckMapper;
    private final CardMapper cardMapper;
    private final CardRepository cardRepository;
    private final DeckCardsRepository deckCardsRepository;

    public DeckServiceImpl(DeckRepository deckRepository, DeckMapper deckMapper, CardMapper cardMapper, CardRepository cardRepository, DeckCardsRepository deckCardsRepository) {
        this.deckRepository = deckRepository;
        this.deckMapper = deckMapper;
        this.cardMapper = cardMapper;
        this.cardRepository = cardRepository;
        this.deckCardsRepository = deckCardsRepository;
    }

    @Override
    public DeckResponse createDeck(CreateDeckRequest createRequest, Integer userId)
    {
        Deck.DeckBuilder deckBuilder = Deck.builder();
        deckBuilder.name(createRequest.getTitle());
        deckBuilder.description(createRequest.getDescription());
        deckBuilder.visibility(createRequest.getVisibility());
        deckBuilder.deckImageUrl(createRequest.getDeckImageUrl());
        Deck deck = deckBuilder.build();

        if (deck.isValidForCreate()) {
            DeckEntity deckEntity = deckMapper.toEntity(deck);
            deckEntity.getCreatorId().setId(userId);
            DeckEntity savedDeck = deckRepository.save(deckEntity);
            return deckMapper.toResponse(deckMapper.toDomain(savedDeck));
        } else {
            throw new IllegalArgumentException("Deck must have a title.");
        }
    }

    @Override
    public DeckPageResponse getDeckPage(Integer deckId)
    {
        DeckEntity deck = deckRepository.findById(deckId).orElseThrow(() -> new NotFoundException("Deck not found"));
        List<DeckCardsEntity> deckCards = deckCardsRepository.findAllByDeckId(deck).orElse(null);

        return DeckPageResponse.builder()
                .deck(deckMapper.toResponse(deckMapper.toDomain(deck)))
                .cards(deckCards != null? deckCards.stream().map(dc -> {
                    CardEntity card = cardRepository.findById(dc.getCardId().getId()).orElseThrow(() -> new NotFoundException("Card not found"));
                    return cardMapper.toDeckCardResponse(card, deckId, dc.getCardCount());
                }).toList() : null)
                .build();
    }

    @Override
    public Page<DeckResponse> getDecksByUser(Integer userId, Pageable pageable)
    {
        return deckRepository.findAllByCreatorId_Id(userId, pageable)
                .map(deckMapper::toDomain)
                .map(deckMapper::toResponse);
    }

    @Override
    public List<DeckCardResponse> getDeckCardsByCardIdAndUserId(Integer cardId, Integer userId)
    {
        List<DeckCardResponse> responses = new ArrayList<>();

        for (DeckEntity deckEntity : deckRepository.findAllByCreatorId_Id(userId)) {
            deckCardsRepository.findAllByCardId_IdAndDeckId(cardId, deckEntity)
                    .ifPresent(deckCard -> responses.add(cardMapper.toDeckCardResponse(cardMapper.deckCardToDomain(deckCard))));
        }

        return responses;
    }

    @Override
    public Page<DeckResponse> getPublicDecksByUser(Integer userId, Pageable pageable)
    {
        return deckRepository.findAllByCreatorId_IdAndVisibility(userId, Visibility.PUBLIC, pageable)
                .map(deckMapper::toDomain)
                .map(deckMapper::toResponse);
    }

    @Override
    public Page<DeckResponse> getVisibleDecks(Integer userId, Pageable pageable) {
        if (userId != null) {
            return deckRepository.findAllByVisibilityOrCreatorId_Id(Visibility.PUBLIC, userId, pageable)
                    .map(deckMapper::toDomain)
                    .map(deckMapper::toResponse);
        } else {
            return deckRepository.findAllByVisibility(Visibility.PUBLIC, pageable)
                    .map(deckMapper::toDomain)
                    .map(deckMapper::toResponse);
        }
    }

    @Override
    public DeckCardResponse updateDeckCard(Integer deckId, DeckCardUpdateRequest updateRequest)
    {
        if (updateRequest == null) {
            throw new BadRequestException("Update request body is required");
        }
        if (updateRequest.getCardId() == null) {
            throw new BadRequestException("Card id is required");
        }
        if (updateRequest.getCount() == null) {
            throw new BadRequestException("Card count is required");
        }
        if (updateRequest.getCount() < 0) {
            throw new BadRequestException("Card count cannot be negative");
        }

        DeckEntity deckEntity = deckRepository.findById(deckId).orElseThrow(() -> new NotFoundException("Deck not found"));
        CardEntity cardEntity = cardRepository.findById(updateRequest.getCardId()).orElseThrow(() -> new NotFoundException("Card not found"));

        var opt = deckCardsRepository.findByDeckId_IdAndCardId_Id(deckId, updateRequest.getCardId());

        if (opt.isPresent())
        //update existing deck_cards_row
        {
            DeckCardsEntity existing = opt.get();
            if (updateRequest.getCount() == 0) {
                deckCardsRepository.delete(existing);
                return getDeckCardResponse(cardEntity, deckEntity, updateRequest.getCount());
            }

            existing.setCardCount(updateRequest.getCount());
            deckCardsRepository.save(existing);
            return getDeckCardResponse(cardEntity, deckEntity, updateRequest.getCount());
        }

        if (updateRequest.getCount() == 0) {
            return getDeckCardResponse(cardEntity, deckEntity, 0);
        }

        DeckCardsEntity created = DeckCardsEntity.builder()
                .deckId(deckEntity)
                .cardId(cardEntity)
                .cardCount(updateRequest.getCount())
                .build();
        deckCardsRepository.save(created);
        return getDeckCardResponse(cardEntity, deckEntity, updateRequest.getCount());
    }

    @Override
    public DeckResponse updateDeckByUserId(Integer deckId,  UpdateDeckRequest updateRequest)
    {
        DeckEntity deckEntity = deckRepository.findById(deckId)
                .orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (updateRequest.getTitle() != null) {
            deckEntity.setDeckName(updateRequest.getTitle());
        }
        if (updateRequest.getDescription() != null) {
            deckEntity.setDeckDescription(updateRequest.getDescription());
        }
        if (updateRequest.getVisibility() != null) {
            deckEntity.setVisibility(updateRequest.getVisibility());
        }
        if (updateRequest.getDeckImageUrl() != null) {
            deckEntity.setDeckImageUrl(updateRequest.getDeckImageUrl());
        }

        DeckEntity savedEntity = deckRepository.save(deckEntity);
        return deckMapper.toResponse(deckMapper.toDomain(savedEntity));
    }

    private DeckCardResponse getDeckCardResponse(CardEntity card, DeckEntity deck, Integer count)
    {
        Integer deckId = deck != null ? deck.getId() : null;
        return cardMapper.toDeckCardResponse(card, deckId, count);
    }
}
