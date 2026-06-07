package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Collection;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.mapper.CollectionMapper;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import com.fhict.hololiveocgmanager.repository.CollectionCardsRepository;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.dto.response.KeywordResponse;
import com.fhict.hololiveocgmanager.dto.response.TagResponse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final CollectionMapper collectionMapper;
    private final UserMapper userMapper;

    public CollectionServiceImpl(CollectionRepository collectionRepository,
                                 CollectionCardsRepository collectionCardsRepository, CardService cardService,
                                 CardMapper cardMapper, CollectionMapper collectionMapper, UserMapper userMapper) {
        this.collectionRepository = collectionRepository;
        this.collectionCardsRepository = collectionCardsRepository;
        this.cardService = cardService;
        this.cardMapper = cardMapper;
        this.collectionMapper = collectionMapper;
        this.userMapper = userMapper;
    }

    public record CardCounts(Integer totalCards, Integer totalCount) {}

    @Override
    @Transactional(readOnly = true)
    public CollectionResponse getCollectionByUserId(Integer userId, Optional<UserEntity> currentUser){
        CollectionEntity collectionEntity = collectionRepository.findByOwnerId_Id(userId)
                .orElseThrow(() -> new NotFoundException("Collection not found for user"));

        if (currentUser.isPresent() && collectionEntity.getVisibility() == Visibility.PRIVATE && !Objects.equals(collectionEntity.getOwnerId().getId(), currentUser.get().getId())) {
            throw new ForbiddenException("You do not have permission to view this collection");
        }
        if (collectionEntity.getVisibility() == Visibility.PRIVATE && currentUser.isEmpty()) {
            throw new ForbiddenException("You do not have permission to view this collection");
        }

        CardCounts counts = getCardCounts(collectionEntity.getId());

        return collectionMapper.toResponse(collectionMapper.toDomain(collectionEntity, counts.totalCards, counts.totalCount));
    }

    @Override
    public Page<CollectionCardResponse> getCollectionCards(Integer userId, Optional<UserEntity> currentUser, Pageable pageable)
    {
        CollectionEntity collectionEntity = collectionRepository.findByOwnerId_Id(userId)
                .orElseThrow(() -> new NotFoundException("Collection not found for user"));

        if (currentUser.isPresent() && collectionEntity.getVisibility() == Visibility.PRIVATE && !Objects.equals(collectionEntity.getOwnerId().getId(), currentUser.get().getId())) {
                throw new ForbiddenException("You do not have permission to view this collection");
            }
        if (collectionEntity.getVisibility() == Visibility.PRIVATE && currentUser.isEmpty()) {
            throw new ForbiddenException("You do not have permission to view this collection");
        }

        CardCounts counts = getCardCounts(collectionEntity.getId());

        return collectionCardsRepository.findByCollectionId_Id(collectionEntity.getId(), pageable)
                .map(collectionCard -> toCardResponse(collectionCard, collectionMapper.toDomain(collectionEntity, counts.totalCards, counts.totalCount)));
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionCardResponse getCollectionCardByUserIdAndCardId(Integer userId, Integer cardId, Optional<UserEntity> currentUser)
    {
        CollectionEntity collectionEntity = collectionRepository.findByOwnerId_Id(userId)
                .orElseThrow(() -> new NotFoundException("Collection not found for user"));

        if (currentUser.isPresent() && collectionEntity.getVisibility() == Visibility.PRIVATE && !Objects.equals(collectionEntity.getOwnerId().getId(), currentUser.get().getId())) {
            throw new ForbiddenException("You do not have permission to view this collection");
        }
        if (collectionEntity.getVisibility() == Visibility.PRIVATE && currentUser.isEmpty()) {
            throw new ForbiddenException("You do not have permission to view this collection");
        }

        Integer collectionId = collectionEntity.getId();

        CollectionCardsEntity collectionCard = collectionCardsRepository
                .findByCollectionId_IdAndCardId_Id(collectionId, cardId)
                .orElseThrow(() -> new NotFoundException("Card not found in collection"));

        CardCounts counts = getCardCounts(collectionId);

        return toCardResponse(collectionCard, collectionMapper.toDomain(collectionEntity, counts.totalCards, counts.totalCount));
    }

    @Override
    @Transactional
    public CollectionCardResponse updateCollectionCardByUserId(Integer userId, Integer collectionId, Integer cardId, Integer count) {
        if (cardId == null) {
            throw new BadRequestException("cardId is required");
        }

        if (count == null || count < 0) {
            throw new BadRequestException("count must be non-negative");
        }

        CollectionEntity collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Collection not found"));

        CardEntity card = cardMapper.toEntity(cardService.getCardByCardId(cardId));

        var opt = collectionCardsRepository.findByCollectionId_IdAndCardId_Id(collection.getId(), cardId);

        if (opt.isPresent()) {
            // update existing collection_cards row
            CollectionCardsEntity existing = opt.get();
            if (count == 0) {
                collectionCardsRepository.delete(existing);
                return getCollectionCardResponse(card);
            } else {
                existing.setCardCount(count);
                var saved = collectionCardsRepository.save(existing);
                CardCounts counts = getCardCounts(collection.getId());
                return toCardResponse(saved, collectionMapper.toDomain(collection, counts.totalCards, counts.totalCount));
            }
        } else {
            // insert new collection_cards row
            if (count == 0) {
                // nothing to create, return minimal response
                return getCollectionCardResponse(card);
            }

            CollectionCardsEntity created = CollectionCardsEntity.builder()
                    .collectionId(collection)
                    .cardId(card)
                    .cardCount(count)
                    .build();

            var saved = collectionCardsRepository.save(created);
            CardCounts counts = getCardCounts(collection.getId());
            return toCardResponse(saved, collectionMapper.toDomain(collection, counts.totalCards, counts.totalCount));
        }
    }

    private CardCounts getCardCounts(Integer collectionId) {
        List<CollectionCardsEntity> collectionCardsEntities =
                collectionCardsRepository.findByCollectionId_Id(collectionId);

        Integer totalCards = collectionCardsEntities.size();
        Integer totalCount = 0;

        for (CollectionCardsEntity cce : collectionCardsEntities) {
            totalCount += cce.getCardCount();
        }

        return new CardCounts(totalCards, totalCount);
    }

    private CollectionCardResponse getCollectionCardResponse(CardEntity card) {
        return CollectionCardResponse.builder()
                .id(card != null ? card.getId() : null)
                .collection(null)
                .cardId(card != null ? card.getCardid() : null)
                .name(card != null ? card.getHolomem() : null)
                .imageUrl(card != null ? card.getImage() : null)
                .cardCount(0)
                .build();
    }

    private CollectionCardResponse toCardResponse(CollectionCardsEntity collectionCard, Collection collection) {
        return CollectionCardResponse.builder()
                .id(collectionCard.getCardId() != null ? collectionCard.getCardId().getId() : null)
                .collection(collectionMapper.toResponse(collection))
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
