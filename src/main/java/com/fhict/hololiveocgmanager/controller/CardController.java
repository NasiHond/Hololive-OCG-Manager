package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.dto.response.CardResponse;
import com.fhict.hololiveocgmanager.repository.*;
import com.fhict.hololiveocgmanager.service.CardService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardRepository cardRepository;
    private final ArtRepository artRepository;
    private final ArtcostRepository artcostRepository;
    private final CardartRepository cardartRepository;
    private final KeywordRepository keywordRepository;
    private final CardkeywordRepository cardkeywordRepository;
    private final TagRepository tagRepository;
    private final CardtagRepository cardtagRepository;
    private final CardTypeRepository cardTypeRepository;
    private final ExtraRepository extraRepository;
    private final ColourRepository colourRepository;
    private final CardService cardService;

    public  CardController(CardRepository cardRepository, ArtRepository artRepository, ArtcostRepository artcostRepository, CardartRepository cardartRepository, KeywordRepository keywordRepository, CardkeywordRepository cardkeywordRepository, TagRepository tagRepository, CardtagRepository cardtagRepository, CardTypeRepository cardTypeRepository, ExtraRepository extraRepository, ColourRepository colourRepository, CardService cardService)
    {
        this.cardRepository = cardRepository;
        this.artRepository = artRepository;
        this.artcostRepository = artcostRepository;
        this.cardartRepository = cardartRepository;
        this.keywordRepository = keywordRepository;
        this.cardkeywordRepository = cardkeywordRepository;
        this.tagRepository = tagRepository;
        this.cardtagRepository = cardtagRepository;
        this.cardTypeRepository = cardTypeRepository;
        this.extraRepository = extraRepository;
        this.colourRepository = colourRepository;
        this.cardService = cardService;
    }

    @PostMapping
    public List<CardResponse> findAllCards()
    {
        return cardService.getAllCards().stream()
                .map(this::toResponse)
                .toList();
    }

    private CardResponse toResponse(Card card)
    {
        return CardResponse.builder()
                .Id(card.getID())
                .cardId(card.getCardID())
                .cardSet(card.getCardset())
                .cardTypeId(card.getCardTypeID())
                .cardTypeName(card.getCardTypeName())
                .cardColour(card.getCardColour())
                .batonpass(card.getBatonpass())
                .holomem(card.getHolomem())
                .bloomLvl(card.getBloomLvl())
                .hp(card.getHp())
                .rarity(card.getRarity())
                .imageURL(card.getImageURL())
                .extraEffect(card.getExtraEffect())
                .arts(card.getArts())
                .build();
    }
}
