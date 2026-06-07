package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.entity.ArtEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtRepository extends JpaRepository<ArtEntity, Integer> {
}
