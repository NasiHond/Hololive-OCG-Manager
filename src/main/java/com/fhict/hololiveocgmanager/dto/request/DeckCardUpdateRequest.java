package com.fhict.hololiveocgmanager.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeckCardUpdateRequest {
    private Integer cardId;
    private Integer count;
}
