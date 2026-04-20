package com.fhict.hololiveocgmanager.specification;

import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.CardtypeEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.repository.CardRepository;
import com.fhict.hololiveocgmanager.repository.CardTypeRepository;
import com.fhict.hololiveocgmanager.repository.ColourRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class CardSpecificationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private ColourRepository colourRepository;

    @Test
    void blankParallelFilterBehavesLikeNormalSearch() {
        seedCards();

        var results = cardRepository.findAll(CardSpecification.withFilters(
                null, null, null, null, null, null, null, ""), Sort.by("id"));

        assertEquals(3, results.size());
    }

    @Test
    void parallelTrueReturnsOnlyDuplicateCardIds() {
        seedCards();

        var results = cardRepository.findAll(CardSpecification.withFilters(
                null, null, null, null, null, null, null, "true"), Sort.by("id"));

        assertEquals(2, results.size());
        assertEquals(List.of("PAR-001", "PAR-001"), results.stream().map(CardEntity::getCardid).toList());
    }

    @Test
    void parallelFalseReturnsOnlyFirstCardPerCardId() {
        var seededCards = seedCards();

        var results = cardRepository.findAll(CardSpecification.withFilters(
                null, null, null, null, null, null, null, "false"), Sort.by("id"));

        assertEquals(2, results.size());
        assertEquals(List.of(seededCards.get(0).getId(), seededCards.get(2).getId()),
                results.stream().map(CardEntity::getId).toList());
        assertEquals(List.of("PAR-001", "UNQ-002"), results.stream().map(CardEntity::getCardid).toList());
    }

    private List<CardEntity> seedCards() {
        var cardType = cardTypeRepository.save(CardtypeEntity.builder()
                .name("Holomem")
                .build());
        var colour = colourRepository.save(ColourEntity.builder()
                .colour("Blue")
                .imageUrl("blue.png")
                .build());

        var firstParallel = cardRepository.save(card("PAR-001", cardType, colour, 100, "Set A"));
        var secondParallel = cardRepository.save(card("PAR-001", cardType, colour, 110, "Set B"));
        var unique = cardRepository.save(card("UNQ-002", cardType, colour, 120, "Set C"));

        return List.of(firstParallel, secondParallel, unique);
    }

    private CardEntity card(String cardId, CardtypeEntity cardType, ColourEntity colour, int hp, String cardSet) {
        return CardEntity.builder()
                .cardid(cardId)
                .cardset(cardSet)
                .cardtype(cardType)
                .cardcolour(colour)
                .batonpass("No")
                .holomem("Holomem")
                .bloomlvl("1")
                .hp(hp)
                .rarity("Common")
                .image("image.png")
                .build();
    }
}

