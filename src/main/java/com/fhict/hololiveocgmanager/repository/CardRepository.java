package com.fhict.hololiveocgmanager.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fhict.hololiveocgmanager.entity.CardEntity;

public interface CardRepository extends JpaRepository<CardEntity, Integer>, JpaSpecificationExecutor<CardEntity> {
	@EntityGraph(attributePaths = {"keywords", "cardtags", "cardtags.tagid", "cardarts", "cardarts.artid", "cardarts.artid.artcosts", "cardarts.artid.critColour", "cardcolour", "cardtype", "extra"})
	Page<CardEntity> findAll(Pageable pageable);

	@EntityGraph(attributePaths = {"keywords", "cardtags", "cardtags.tagid", "cardarts", "cardarts.artid", "cardarts.artid.artcosts", "cardarts.artid.critColour", "cardcolour", "cardtype", "extra"})
	Optional<CardEntity> findById(Integer id);

	Optional<CardEntity> findByCardidIgnoreCaseAndCardsetIgnoreCaseAndRarityIgnoreCase(String cardid, String cardset, String rarity);
}
