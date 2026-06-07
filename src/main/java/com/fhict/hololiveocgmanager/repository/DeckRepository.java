package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeckRepository extends JpaRepository<DeckEntity, Integer>
{
    Page<DeckEntity> findAllByCreatorId_Id(@NonNull Integer userId, Pageable pageable);
    List<DeckEntity> findAllByCreatorId_Id(@NonNull Integer userId);
    Page<DeckEntity> findAllByCreatorId_IdAndVisibility(@NonNull Integer userId, Visibility visibility, Pageable pageable);
    Page<DeckEntity> findAllByVisibilityOrCreatorId_Id(Visibility visibility, @NonNull Integer userId, Pageable pageable);
    Page<DeckEntity> findAllByVisibility(Visibility visibility, Pageable pageable);
}
