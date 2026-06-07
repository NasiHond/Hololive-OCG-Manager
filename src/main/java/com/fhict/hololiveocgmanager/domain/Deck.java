package com.fhict.hololiveocgmanager.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Deck {
    private Integer id;
    private String name;
    private User owner;
    private String description;
    @Builder.Default
    private Visibility visibility = Visibility.PRIVATE;
    private String deckImageUrl;

    public Boolean isValidForCreate()
    {
        return name != null && !name.isEmpty() && visibility != null;
    }
}
