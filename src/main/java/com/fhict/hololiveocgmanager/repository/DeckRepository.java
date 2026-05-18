package com.fhict.hololiveocgmanager.repository;

import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface DeckRepository extends CrudRepository<DeckEntity, Integer>
{
    Page<DeckEntity> findAllByCreatorId_Id(@NonNull Integer userId, Pageable pageable);

    Page<DeckEntity> findAllByCreatorId_IdAndVisibility(@NonNull Integer userId, Visibility visibility, Pageable pageable);
}
