package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fhict.hololiveocgmanager.entity.ColourEntity;

public interface ColourRepository extends CrudRepository<ColourEntity, Integer> {
	Optional<ColourEntity> findByColourIgnoreCase(String colour);
}
