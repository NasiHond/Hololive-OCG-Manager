package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.repository.CollectionCardsRepository;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.dto.response.KeywordResponse;
import com.fhict.hololiveocgmanager.dto.response.TagResponse;

import java.util.List;
import java.util.Objects;

import com.fhict.hololiveocgmanager.repository.CollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fhict.hololiveocgmanager.exception.BadRequestException;
import com.fhict.hololiveocgmanager.exception.ForbiddenException;
import com.fhict.hololiveocgmanager.exception.NotFoundException;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionCardsRepository collectionCardsRepository;
    private final CardService cardService;
    private final CardMapper cardMapper;

    public CollectionServiceImpl(CollectionRepository collectionRepository,
                                 CollectionCardsRepository collectionCardsRepository, CardService cardService,
                                 CardMapper cardMapper) {
        this.collectionRepository = collectionRepository;
        this.collectionCardsRepository = collectionCardsRepository;
        this.cardService = cardService;
        this.cardMapper = cardMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionCardsPageResponse getCollectionByUserId(Integer userId, int page, int size) {
        CollectionEntity collection = collectionRepository.findByOwnerId_Id(userId)
                .orElseThrow(() -> new NotFoundException("Collection not found for user"));

        Integer collectionId = collection.getId();

        Pageable pageable = PageRequest.of(page, size);
        Page<CollectionCardsEntity> collectionCardsPage = collectionCardsRepository.findByCollectionId_Id(collectionId, pageable);

        CollectionResponse collectionResponse = CollectionResponse.builder()
                .id(collection.getId())
                .ownerId(collection.getOwnerId() != null ? collection.getOwnerId().getId() : null)
                .visibility(collection.getVisibility())
                .totalCards(Math.toIntExact(collectionCardsPage.getTotalElements()))
                .totalCount(Math.toIntExact(collectionCardsRepository.sumCardCountByCollectionId_Id(collectionId)))
                .build();

        return CollectionCardsPageResponse.builder()
                .collection(collectionResponse)
                .cards(collectionCardsPage.map(this::toCardResponse).getContent())
                .page(page)
                .size(size)
                .last(collectionCardsPage.isLast())
                .totalElements(Math.toIntExact(collectionCardsPage.getTotalElements()))
                .hasMore(collectionCardsPage.hasNext())
                .totalPages(collectionCardsPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionCardResponse getCollectionCardByUserIdAndCardId(Integer userId, Integer cardId)
    {
        CollectionEntity collection = collectionRepository.findByOwnerId_Id(userId)
                .orElseThrow(() -> new NotFoundException("Collection not found for user"));

        Integer collectionId = collection.getId();

        CollectionCardsEntity collectionCard = collectionCardsRepository
                .findByCollectionId_IdAndCardId_Id(collectionId, cardId)
                .orElseThrow(() -> new NotFoundException("Card not found in collection"));

        return toCardResponse(collectionCard);
    }

    @Override
    @Transactional
    public CollectionCardResponse updateCollectionCardByUserId(Integer userId, Integer collectionId, Integer cardId, Integer amount) {
        if (cardId == null) {
            throw new BadRequestException("cardId is required");
        }

        if (amount == null || amount < 0) {
            throw new BadRequestException("amount must be non-negative");
        }

        CollectionEntity collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Collection not found"));

        if (!Objects.equals(collection.getOwnerId().getId(), userId)) {
            throw new ForbiddenException("You do not have permission to modify this collection");
        }

        CardEntity card = cardMapper.toEntity(cardService.getCardByCardId(cardId));

        var opt = collectionCardsRepository.findByCollectionId_IdAndCardId_Id(collection.getId(), cardId);

        if (opt.isPresent()) {
            // update existing collection_cards row
            CollectionCardsEntity existing = opt.get();
            if (amount == 0) {
                collectionCardsRepository.delete(existing);
                return getCollectionCardResponse(card);
            } else {
                existing.setCardCount(amount);
                var saved = collectionCardsRepository.save(existing);
                return toCardResponse(saved);
            }
        } else {
            // insert new collection_cards row
            if (amount == 0) {
                // nothing to create, return minimal response
                return getCollectionCardResponse(card);
            }

            CollectionCardsEntity created = CollectionCardsEntity.builder()
                    .collectionId(collection)
                    .cardId(card)
                    .cardCount(amount)
                    .build();

            var saved = collectionCardsRepository.save(created);
            return toCardResponse(saved);
        }
    }

    private CollectionCardResponse getCollectionCardResponse(CardEntity card) {
        return CollectionCardResponse.builder()
                .id(card != null ? card.getId() : null)
                .collectionCardId(null)
                .cardId(card != null ? card.getCardid() : null)
                .name(card != null ? card.getHolomem() : null)
                .imageUrl(card != null ? card.getImage() : null)
                .cardCount(0)
                .build();
    }

    private CollectionCardResponse toCardResponse(CollectionCardsEntity collectionCard) {
        return CollectionCardResponse.builder()
                .id(collectionCard.getCardId() != null ? collectionCard.getCardId().getId() : null)
                .collectionCardId(collectionCard.getId())
                .cardId(collectionCard.getCardId() != null ? collectionCard.getCardId().getCardid() : null)
                .name(collectionCard.getCardId() != null ? collectionCard.getCardId().getHolomem() : null)
                .imageUrl(collectionCard.getCardId() != null ? collectionCard.getCardId().getImage() : null)
                .cardCount(collectionCard.getCardCount())
                .rarity(collectionCard.getCardId() != null ? collectionCard.getCardId().getRarity() : null)
                .cardSet(collectionCard.getCardId() != null ? collectionCard.getCardId().getCardset() : null)
                .cardTypeName(collectionCard.getCardId() != null && collectionCard.getCardId().getCardtype() != null
                        ? collectionCard.getCardId().getCardtype().getName()
                        : null)
                .cardColour(collectionCard.getCardId() != null && collectionCard.getCardId().getCardcolour() != null
                        ? collectionCard.getCardId().getCardcolour().getColour()
                        : null)
                .batonpass(collectionCard.getCardId() != null ? collectionCard.getCardId().getBatonpass() : null)
                .holomem(collectionCard.getCardId() != null ? collectionCard.getCardId().getHolomem() : null)
                .bloomLvl(collectionCard.getCardId() != null ? collectionCard.getCardId().getBloomlvl() : null)
                .hp(collectionCard.getCardId() != null ? collectionCard.getCardId().getHp() : null)
                .extraEffect(collectionCard.getCardId() != null && collectionCard.getCardId().getExtra() != null
                        ? collectionCard.getCardId().getExtra().getEffect()
                        : null)
                .keyword(collectionCard.getCardId() != null && collectionCard.getCardId().getKeywords() != null && !collectionCard.getCardId().getKeywords().isEmpty()
                        ? collectionCard.getCardId().getKeywords().stream()
                                .findFirst()
                                .map((KeywordEntity k) -> KeywordResponse.builder()
                                        .id(k.getId())
                                        .type(k.getType())
                                        .name(k.getName())
                                        .effect(k.getEffect())
                                        .build())
                                .orElse(null)
                        : null)
                .arts(collectionCard.getCardId() != null ? cardMapper.mapArtsToResponse(collectionCard.getCardId()) : List.of())
                .tags(collectionCard.getCardId() != null && collectionCard.getCardId().getCardtags() != null
                        ? collectionCard.getCardId().getCardtags().stream()
                                .map(CardtagEntity::getTagid)
                                .map(tag -> TagResponse.builder()
                                        .id(tag.getId())
                                        .name(tag.getName())
                                        .build())
                                .toList()
                        : List.of())
                .build();
    }
}
