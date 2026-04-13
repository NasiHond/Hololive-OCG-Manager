package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fhict.hololiveocgmanager.entity.CardEntity;

public interface CardRepository extends CrudRepository<CardEntity, Integer> {
	Optional<CardEntity> findByCardidIgnoreCaseAndCardsetIgnoreCaseAndRarityIgnoreCase(String cardid, String cardset, String rarity);
}
