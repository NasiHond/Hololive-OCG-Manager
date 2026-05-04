package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;

public interface CollectionService {
    CollectionCardsPageResponse getCollectionByUserId(Integer userId, int page, int size);
}
