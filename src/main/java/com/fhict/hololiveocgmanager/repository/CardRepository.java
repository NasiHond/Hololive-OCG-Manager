package com.fhict.hololiveocgmanager.repository;

import org.springframework.data.repository.CrudRepository;

import com.fhict.hololiveocgmanager.entity.CardEntity;

public interface CardRepository extends CrudRepository<CardEntity, Integer> {
}
