package com.fhict.hololiveocgmanager.dto.response;

import lombok.*;

import java.util.List;

@Data
@Builder
public class CardResponse {
    private Integer Id;
    private String cardId;
    private String cardSet;
    private Integer cardTypeId;
    private String cardTypeName;
    private String cardColour;
    private String batonpass;
    private String holomem;
    private String bloomLvl;
    private Integer hp;
    private String rarity;
    private String imageURL;
    private String extraEffect;
    private List<ArtResponse> arts;
}
