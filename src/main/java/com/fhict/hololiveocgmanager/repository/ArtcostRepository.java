package com.fhict.hololiveocgmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fhict.hololiveocgmanager.entity.ArtEntity;
import com.fhict.hololiveocgmanager.entity.ArtcostEntity;

public interface ArtcostRepository extends JpaRepository<ArtcostEntity, Integer> {
    List<ArtcostEntity> findAllByArt(ArtEntity art);
}
