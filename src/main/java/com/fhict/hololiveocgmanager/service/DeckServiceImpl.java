package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Deck;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.request.CreateDeckRequest;
import com.fhict.hololiveocgmanager.dto.request.UpdateDeckRequest;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.mapper.DeckMapper;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DeckServiceImpl implements DeckService
{
    private final DeckRepository deckRepository;
    private final DeckMapper deckMapper;

    public DeckServiceImpl(DeckRepository deckRepository, DeckMapper deckMapper) {
        this.deckRepository = deckRepository;
        this.deckMapper = deckMapper;
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
    public Page<DeckResponse> getDecksByUser(Integer userId, Pageable pageable)
    {
        return deckRepository.findAllByCreatorId_Id(userId, pageable)
                .map(deckMapper::toDomain)
                .map(deckMapper::toResponse);
    }

    @Override
    public Page<DeckResponse> getPublicDecksByUser(Integer userId, Pageable pageable)
    {
        return deckRepository.findAllByCreatorId_IdAndVisibility(userId, Visibility.PUBLIC, pageable)
                .map(deckMapper::toDomain)
                .map(deckMapper::toResponse);
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
}
