package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface CardService
{
    Card createCard(Card card);

    Card getCard(Card card);

    List<Card> searchCards(Specification<CardEntity> spec);

    Card getCardByCardId(Integer cardId);

    List<Card> getAllCards();

    Card updateCard(Card card);

    void deleteCard(Card card);
}
