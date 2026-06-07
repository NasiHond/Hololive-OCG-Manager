package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CollectionService {
    CollectionResponse getCollectionByUserId(Integer userId, Optional<UserEntity> currentUser);
    Page<CollectionCardResponse> getCollectionCards(Integer userId, Optional<UserEntity> currentUser, Pageable pageable);

    CollectionCardResponse getCollectionCardByUserIdAndCardId(Integer userId, Integer cardId, Optional<UserEntity> currentUser);

    CollectionCardResponse updateCollectionCardByUserId(Integer userId, Integer collectionId, Integer cardId, Integer amount);

}
