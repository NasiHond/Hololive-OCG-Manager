package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;

public interface CollectionService {
    CollectionCardsPageResponse getCollectionByUserId(Integer userId, int page, int size);

    CollectionCardResponse getCollectionCardByUserIdAndCardId(Integer userId, Integer cardId);

    CollectionCardResponse updateCollectionCardByUserId(Integer userId, Integer collectionId, Integer cardId, Integer amount);

}
