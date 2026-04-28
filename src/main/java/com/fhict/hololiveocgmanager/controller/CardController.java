package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.dto.response.CardResponse;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.repository.*;
import com.fhict.hololiveocgmanager.service.CardService;
import com.fhict.hololiveocgmanager.specification.CardSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CardController {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardController(CardRepository cardRepository, ArtRepository artRepository, ArtcostRepository artcostRepository, CardartRepository cardartRepository, KeywordRepository keywordRepository, CardkeywordRepository cardkeywordRepository, TagRepository tagRepository, CardtagRepository cardtagRepository, CardTypeRepository cardTypeRepository, ExtraRepository extraRepository, ColourRepository colourRepository, CardService cardService, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
    }

    /**
     * Get all cards with pagination.
     * GET /api/cards?page=0&size=20
     */
    @GetMapping
    public Page<CardResponse> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAll(pageable)
                .map(this::entityToDomain)
                .map(this::toResponse);
    }

    /**
     * Search and filter cards with optional parameters.
     * GET /api/cards/search?cardName=Ina&bloomLvl=3&colour=Blue&cardSet=HoloX
     * All parameters are optional; null filters are ignored.
     */
    @GetMapping("/search")
    public Page<CardResponse> searchCards(
            @RequestParam(required = false) String cardName,
            @RequestParam(required = false) String bloomLvl,
            @RequestParam(required = false) String colour,
            @RequestParam(required = false) String cardSet,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String cardType,
            @RequestParam(required = false) String holomem,
            @RequestParam(required = false) String parallel,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        var normalizedParallel = parallel == null || parallel.isBlank() ? null : parallel.trim();
        var spec = CardSpecification.withFilters(cardName, bloomLvl, colour, cardSet, rarity, cardType, holomem, normalizedParallel);
        return cardRepository.findAll(spec, pageable)
                .map(this::entityToDomain)
                .map(this::toResponse);
    }

    /**
     * Get a single card by ID.
     */
    @GetMapping("/{id}")
    public CardResponse getCardById(@PathVariable int id) {
        return cardRepository.findById(id)
                .map(this::entityToDomain)
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Card not found"));
    }


    private Card entityToDomain(com.fhict.hololiveocgmanager.entity.CardEntity entity) {
        return cardMapper.toDomain(entity);
    }

    private CardResponse toResponse(Card card) {
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
                .arts(card.getArts() != null ? 
                    card.getArts().stream()
                        .map(art -> new com.fhict.hololiveocgmanager.dto.response.ArtResponse(
                            art.getID(),
                            art.getName(),
                            art.getEffect(),
                            art.getDamage(),
                            art.getCritColourName(),
                            art.getCosts() != null ?
                                art.getCosts().stream()
                                    .map(cost -> new com.fhict.hololiveocgmanager.dto.response.ArtcostResponse(
                                        cost.getId(),
                                        cost.getAmount(),
                                        cost.getColour() != null ? cost.getColour().getColour() : null,
                                        cost.getColour() != null ? cost.getColour().getImageUrl() : null
                                    ))
                                    .toList()
                                : List.of()
                        ))
                        .toList()
                    : List.of())
                .build();
    }
}
