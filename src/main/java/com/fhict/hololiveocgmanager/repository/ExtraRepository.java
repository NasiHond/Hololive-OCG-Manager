package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import com.fhict.hololiveocgmanager.entity.ExtraEntity;
import org.springframework.data.repository.CrudRepository;

public interface ExtraRepository extends CrudRepository<ExtraEntity, Integer> {
	Optional<ExtraEntity> findByEffectIgnoreCase(String effect);
}
