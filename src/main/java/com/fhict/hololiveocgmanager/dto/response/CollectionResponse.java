package com.fhict.hololiveocgmanager.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionResponse {
    private Integer id;
    private Integer ownerId;
    private Integer visibility;
    private Integer totalCards;
    private Integer totalCount;
}
