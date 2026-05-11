package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.entity.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Integer> {
	Optional<CollectionEntity> findByOwnerId_Id(Integer ownerId);
}

