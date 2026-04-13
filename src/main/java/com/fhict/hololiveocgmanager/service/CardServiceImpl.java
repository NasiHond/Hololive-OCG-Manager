package com.fhict.hololiveocgmanager.service;

import java.util.List;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.repository.CardRepository;
import com.fhict.hololiveocgmanager.repository.CardTypeRepository;
import com.fhict.hololiveocgmanager.repository.ColourRepository;

@Service
public class CardServiceImpl implements CardService
{
    private final CardRepository cardRepository;
    private final CardTypeRepository cardTypeRepository;
    private final ColourRepository colourRepository;
    private final CardMapper cardMapper;

    public CardServiceImpl(CardRepository cardRepository,
                           CardTypeRepository cardTypeRepository,
                           ColourRepository colourRepository,
                           CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.colourRepository = colourRepository;
        this.cardMapper = cardMapper;
    }

    @Override
    public Card createCard(Card card) {
        CardEntity cardEntity = cardMapper.toEntity(card);

        if (cardEntity == null) {
            return null;
        }

        resolveTypeReference(cardEntity);
        resolveColourReference(cardEntity);

        CardEntity saved = cardRepository.save(cardEntity);
        return cardMapper.toDomain(saved);
    }

    @Override
    public Card getCard(Card card) {
        if (card == null || card.getID() == null) {
            return null;
        }

        return cardRepository.findById(card.getID())
                .map(cardMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<Card> getAllCards() {
        return StreamSupport.stream(cardRepository.findAll().spliterator(), false)
                .map(cardMapper::toDomain)
                .toList();
    }

    @Override
    public Card updateCard(Card card) {
        return createCard(card);
    }

    @Override
    public void deleteCard(Card card) {
        if (card == null || card.getID() == null) {
            return;
        }

        cardRepository.deleteById(card.getID());
    }

    private void resolveTypeReference(CardEntity cardEntity) {
        CardtypeEntity type = cardEntity.getCardtype();

        if (type == null || type.getName() == null || type.getName().isBlank()) {
            return;
        }

        CardtypeEntity resolvedType = cardTypeRepository.findByNameIgnoreCase(type.getName().trim())
                .orElseGet(() -> cardTypeRepository.save(CardtypeEntity.builder()
                        .name(type.getName().trim())
                        .build()));

        cardEntity.setCardtype(resolvedType);
    }

    private void resolveColourReference(CardEntity cardEntity) {
        ColourEntity colour = cardEntity.getCardcolour();

        if (colour == null || colour.getColour() == null || colour.getColour().isBlank()) {
            return;
        }

        String colourName = colour.getColour().trim();
        ColourEntity resolvedColour = colourRepository.findByColourIgnoreCase(colourName)
                .orElseGet(() -> colourRepository.save(ColourEntity.builder()
                        .colour(colourName)
                        .imageUrl(colour.getImageUrl())
                        .build()));

        if (resolvedColour.getImageUrl() == null && colour.getImageUrl() != null) {
            resolvedColour.setImageUrl(colour.getImageUrl());
            resolvedColour = colourRepository.save(resolvedColour);
        }

        cardEntity.setCardcolour(resolvedColour);
    }
}
