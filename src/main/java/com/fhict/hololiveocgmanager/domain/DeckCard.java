package com.fhict.hololiveocgmanager.domain;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DeckCard {
    private Integer id;
    private Integer deckId;
    private Integer count;
    private String cardID;
    private String cardset;
    private Integer cardTypeID;
    private String cardTypeName;
    private String cardColour;
    private String batonpass;
    private String holomem;
    private String bloomLvl;
    private Integer hp;
    private String rarity;
    private String imageURL;
    private List<Keyword> keywords;
    private Integer extraID;
    private String extraEffect;
    private List<Art> arts;
    private List<Tag> tags;
}
