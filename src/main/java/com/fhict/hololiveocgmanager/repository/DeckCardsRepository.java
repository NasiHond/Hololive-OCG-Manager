package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.entity.DeckCardsEntity;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckCardsRepository extends JpaRepository<DeckCardsEntity, Integer> {
    Optional<DeckCardsEntity> findByDeckId_IdAndCardId_Id(Integer deckId, Integer cardId);

    Optional<DeckCardsEntity> findAllByCardId_IdAndDeckId(Integer cardId,  DeckEntity deck);

    Optional<List<DeckCardsEntity>> findAllByDeckId(DeckEntity deck);
}
