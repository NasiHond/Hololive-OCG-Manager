package com.fhict.hololiveocgmanager.dto.response;

import com.fhict.hololiveocgmanager.domain.Visibility;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeckResponse {
    private Integer id;
    private String title;
    private Integer ownerId;
    private String ownerName;
    private String deckImageUrl;
    private Visibility visibility;
}
