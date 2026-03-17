package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fhict.hololiveocgmanager.entity.CardEntity;

public interface CardRepository extends CrudRepository<CardEntity, Integer> {
	Optional<CardEntity> findByCardidAndCardsetIgnoreCase(String cardid, String cardset);
}
