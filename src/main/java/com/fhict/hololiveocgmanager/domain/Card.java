package com.fhict.hololiveocgmanager.domain;

import com.fhict.hololiveocgmanager.entity.ArtEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Card {
    private Integer ID;
    private String cardID;
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
    private List<Art> arts;
}
