package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Collection;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.CollectionEntity;
import org.springframework.stereotype.Component;

@Component
public class CollectionMapper {
    private final UserMapper userMapper;

    public CollectionMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CollectionEntity toEntity(Collection collection)
    {
        CollectionEntity.CollectionEntityBuilder builder = CollectionEntity.builder();
        builder.id(collection.getId());
        builder.ownerId(userMapper.toEntity(collection.getOwner()));
        builder.visibility(collection.getVisibility());
        return builder.build();
    }

    public Collection toDomain(CollectionEntity collectionEntity, Integer totalCards, Integer totalCount)
    {
        Collection.CollectionBuilder builder = Collection.builder();
        builder.id(collectionEntity.getId());
        builder.owner(userMapper.toDomain(collectionEntity.getOwnerId()));
        builder.visibility(collectionEntity.getVisibility());
        builder.totalCards(totalCards);
        builder.totalCount(totalCount);
        return builder.build();
    }

    public CollectionResponse toResponse(Collection collection)
    {
        CollectionResponse.CollectionResponseBuilder builder = CollectionResponse.builder();
        builder.id(collection.getId());
        builder.owner(userMapper.toResponse(collection.getOwner()));
        builder.visibility(collection.getVisibility());
        builder.totalCards(collection.getTotalCards());
        builder.totalCount(collection.getTotalCount());
        return builder.build();
    }
}
