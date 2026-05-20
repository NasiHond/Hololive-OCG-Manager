package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.entity.DeckCardsEntity;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface DeckCardsRepository extends CrudRepository<DeckCardsEntity, Integer> {
    Optional<DeckCardsEntity> findByDeckId_IdAndCardId_Id(Integer deckId, Integer cardId);

    Optional<List<DeckCardsEntity>> findAllByDeckId(DeckEntity deck);
}
