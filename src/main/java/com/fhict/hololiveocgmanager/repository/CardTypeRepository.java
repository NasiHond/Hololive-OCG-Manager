package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fhict.hololiveocgmanager.entity.CardtypeEntity;

public interface CardTypeRepository extends CrudRepository<CardtypeEntity, Integer> {
	Optional<CardtypeEntity> findByNameIgnoreCase(String name);
}
