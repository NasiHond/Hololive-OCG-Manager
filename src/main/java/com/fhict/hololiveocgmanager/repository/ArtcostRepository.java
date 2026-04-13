package com.fhict.hololiveocgmanager.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.fhict.hololiveocgmanager.entity.ArtEntity;
import com.fhict.hololiveocgmanager.entity.ArtcostEntity;

public interface ArtcostRepository extends CrudRepository<ArtcostEntity, Integer> {
    List<ArtcostEntity> findAllByArt(ArtEntity art);
}
