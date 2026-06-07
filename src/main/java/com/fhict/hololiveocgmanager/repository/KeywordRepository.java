package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import com.fhict.hololiveocgmanager.entity.KeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<KeywordEntity, Integer> {
	Optional<KeywordEntity> findByTypeIgnoreCaseAndNameIgnoreCaseAndEffectIgnoreCase(String type, String name, String effect);
}
