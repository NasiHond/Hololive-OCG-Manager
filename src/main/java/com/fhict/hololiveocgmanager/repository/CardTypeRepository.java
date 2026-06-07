package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fhict.hololiveocgmanager.entity.CardtypeEntity;

public interface CardTypeRepository extends JpaRepository<CardtypeEntity, Integer> {
	Optional<CardtypeEntity> findByNameIgnoreCase(String name);
}
