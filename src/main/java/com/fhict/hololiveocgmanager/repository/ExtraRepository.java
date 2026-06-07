package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import com.fhict.hololiveocgmanager.entity.ExtraEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExtraRepository extends JpaRepository<ExtraEntity, Integer> {
	Optional<ExtraEntity> findByEffectIgnoreCase(String effect);
}
