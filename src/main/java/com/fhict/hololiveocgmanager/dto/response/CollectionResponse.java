package com.fhict.hololiveocgmanager.dto.response;

import lombok.Builder;
import lombok.Data;
import com.fhict.hololiveocgmanager.domain.Visibility;

@Data
@Builder
public class CollectionResponse {
    private Integer id;
    private Integer ownerId;
    private Visibility visibility;
    private Integer totalCards;
    private Integer totalCount;
}
