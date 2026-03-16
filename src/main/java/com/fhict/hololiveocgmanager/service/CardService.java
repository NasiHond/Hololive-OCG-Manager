package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Card;

import java.util.List;

public interface CardService
{
    Card createCard(Card card);

    Card getCard(Card card);

    List<Card> getCards();

    Card updateCard(Card card);

    void deleteCard(Card card);
}
