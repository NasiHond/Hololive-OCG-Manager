package com.fhict.hololiveocgmanager.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CollectionCardsPageResponse {
    private CollectionResponse collection;
    private List<CollectionCardResponse> cards;
    private Integer page;
    private Integer size;
    private Boolean last;
    private Integer totalElements;
    private Boolean hasMore;
    private Integer totalPages;
}

