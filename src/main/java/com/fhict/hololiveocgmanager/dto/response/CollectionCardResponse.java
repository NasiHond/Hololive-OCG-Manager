package com.fhict.hololiveocgmanager.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CollectionCardResponse {
    private Integer id;
    private Integer collectionCardId;
    private String cardId;
    private String name;
    private String imageUrl;
    private Integer cardCount;
    private String rarity;
    private String cardSet;
    private String cardTypeName;
    private String cardColour;
    private String batonpass;
    private String holomem;
    private String bloomLvl;
    private Integer hp;
    private String extraEffect;
}
