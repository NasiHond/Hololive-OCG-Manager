package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.entity.CollectionCardsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CollectionCardsRepository extends JpaRepository<CollectionCardsEntity, Integer> {
    Page<CollectionCardsEntity> findByCollectionId_Id(Integer collectionId, Pageable pageable);

    @Query("select coalesce(sum(cc.cardCount), 0) from CollectionCardsEntity cc where cc.collectionId.id = :collectionId")
    Long sumCardCountByCollectionId_Id(@Param("collectionId") Integer collectionId);

    Optional<CollectionCardsEntity> findByCollectionId_IdAndCardId_Id(Integer collectionId, Integer cardId);

    void deleteByCollectionId_IdAndCardId_Id(Integer collectionId, Integer cardId);
}

