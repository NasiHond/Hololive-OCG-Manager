package com.fhict.hololiveocgmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Card {
    private Integer ID;
    private Integer cardID;
    private String cardset;
    private Integer cardTypeID;
    private String cardTypeName;
    private String cardColour;
    private String batonpass;
    private String holomem;
    private String bloomLvl;
    private Integer hp;
    private Boolean isBuzz;
    private String rarity;
    private String imageURL;
    private Integer extraID;
    private String extraEffect;
}
