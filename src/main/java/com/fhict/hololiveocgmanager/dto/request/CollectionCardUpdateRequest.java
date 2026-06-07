package com.fhict.hololiveocgmanager.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionCardUpdateRequest {
    private Integer collectionId;
    private Integer cardId;
    private Integer count;
}
