package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fhict.hololiveocgmanager.entity.CardEntity;

public interface CardRepository extends JpaRepository<CardEntity, Integer>, JpaSpecificationExecutor<CardEntity> {
	Optional<CardEntity> findByCardidIgnoreCaseAndCardsetIgnoreCaseAndRarityIgnoreCase(String cardid, String cardset, String rarity);
}
