package com.fhict.hololiveocgmanager.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponse {
    private Integer id;
    private DeckResponse deck;
    private UserResponse user;
    private Double rating;
    private String comment;
}
