package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import com.fhict.hololiveocgmanager.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<TagEntity, Integer> {
	Optional<TagEntity> findByNameIgnoreCase(String name);
}
