package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository  extends JpaRepository<ReviewEntity, Integer> {
    Page<ReviewEntity> getAllByDeckId(Integer deckId, Pageable pageable);

    Integer deck(DeckEntity deck);
}
