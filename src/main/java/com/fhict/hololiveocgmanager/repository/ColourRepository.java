package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fhict.hololiveocgmanager.entity.ColourEntity;

public interface ColourRepository extends JpaRepository<ColourEntity, Integer> {
	Optional<ColourEntity> findByColourIgnoreCase(String colour);
}
