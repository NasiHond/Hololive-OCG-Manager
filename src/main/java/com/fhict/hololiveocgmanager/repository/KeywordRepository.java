package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import com.fhict.hololiveocgmanager.entity.KeywordEntity;
import org.springframework.data.repository.CrudRepository;

public interface KeywordRepository extends CrudRepository<KeywordEntity, Integer> {
	Optional<KeywordEntity> findByTypeIgnoreCaseAndNameIgnoreCaseAndEffectIgnoreCase(String type, String name, String effect);
}
