package com.fhict.hololiveocgmanager.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Review {
    private Integer id;
    private Deck deck;
    private User user;
    private Double rating;
    private String comment;

    public Boolean isValidForCreate()
    {
        return deck != null && user != null && rating != null && rating > 0;
    }
}
