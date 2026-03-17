package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import com.fhict.hololiveocgmanager.entity.TagEntity;
import org.springframework.data.repository.CrudRepository;

public interface TagRepository extends CrudRepository<TagEntity, Integer> {
	Optional<TagEntity> findByNameIgnoreCase(String name);
}
