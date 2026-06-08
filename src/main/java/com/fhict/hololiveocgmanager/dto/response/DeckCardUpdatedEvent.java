package com.fhict.hololiveocgmanager.dto.response;

import com.fhict.hololiveocgmanager.domain.DeckEventType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DeckCardUpdatedEvent {
    private Integer deckId;
    private DeckEventType eventType;
    private DeckCardResponse card;
    private Integer updatedByUserId;
}
