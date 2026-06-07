package com.fhict.hololiveocgmanager.domain;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Collection {
    private Integer id;
    private User owner;
    private Visibility visibility;
    private Integer totalCards;
    private Integer totalCount;
}
