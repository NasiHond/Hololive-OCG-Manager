package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.CollectionCardsEntity;
import com.fhict.hololiveocgmanager.entity.CollectionEntity;
import com.fhict.hololiveocgmanager.repository.CollectionCardsRepository;
import com.fhict.hololiveocgmanager.repository.CollectionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CollectionServiceImpl implements CollectionService {
    private final CollectionRepository collectionRepository;
    private final CollectionCardsRepository collectionCardsRepository;

    public CollectionServiceImpl(CollectionRepository collectionRepository,
                                CollectionCardsRepository collectionCardsRepository) {
        this.collectionRepository = collectionRepository;
        this.collectionCardsRepository = collectionCardsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CollectionCardsPageResponse getCollectionByUserId(Integer userId, int page, int size) {
        CollectionEntity collection = collectionRepository.findByOwnerId_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Collection not found for user"));

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
                .build();
    }

}
