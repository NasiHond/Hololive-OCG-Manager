package com.fhict.hololiveocgmanager.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DeckCardResponse {
    private Integer id;
    private Integer deckId;
    private Integer count;
    private String cardID;
    private String cardset;
    private String cardTypeName;
    private String cardColour;
    private String batonpass;
    private String holomem;
    private String bloomLvl;
    private Integer hp;
    private String rarity;
    private String imageURL;
    private List<KeywordResponse> keywords;
    private Integer extraID;
    private String extraEffect;
    private List<ArtResponse> arts;
    private List<TagResponse> tags;
}
