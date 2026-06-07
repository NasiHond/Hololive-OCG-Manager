package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.request.CreateDeckRequest;
import com.fhict.hololiveocgmanager.dto.request.DeckCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.request.UpdateDeckRequest;
import com.fhict.hololiveocgmanager.dto.response.DeckCardResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckPageResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface DeckService {
    DeckResponse createDeck(CreateDeckRequest createDeckRequest, UserEntity user);

    Page<DeckResponse> getDecksByUser(Integer userId, Pageable pageable);

    DeckResponse getDeckById(Integer deckId);

    List<DeckCardResponse> getDeckCardsByCardIdAndUserId(Integer cardId,  Integer userId);

    Page<DeckResponse> getPublicDecksByUser(Integer userId, Pageable pageable);

    Page<DeckResponse> getVisibleDecks(Integer userId, Pageable pageable);

    DeckPageResponse getDeckPage(Integer deckId);

    DeckCardResponse updateDeckCard(Integer deckId, DeckCardUpdateRequest updateRequest);

    DeckResponse updateDeckByUserId(Integer deckId, UpdateDeckRequest updateDeckRequest);
}
