package com.fhict.hololiveocgmanager.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeckPageResponse {
    private DeckResponse deck;
    private List<DeckCardResponse> cards;
}
