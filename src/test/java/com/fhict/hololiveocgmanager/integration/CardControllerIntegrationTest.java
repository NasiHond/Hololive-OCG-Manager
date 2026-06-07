package com.fhict.hololiveocgmanager.integration;

import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class CardControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardTypeRepository cardTypeRepository;

    @Autowired
    private ColourRepository colourRepository;

    @Autowired
    private ExtraRepository extraRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        cardRepository.deleteAll();
        seedTestData();
    }

    private void seedTestData() {
        // Create test card type
        CardtypeEntity cardType = new CardtypeEntity();
        cardType.setName("Standard");
        cardType = cardTypeRepository.save(cardType);

        // Create test card color
        ColourEntity cardColour = new ColourEntity();
        cardColour.setColour("Blue");
        cardColour.setImageUrl("https://example.com/blue.png");
        cardColour = colourRepository.save(cardColour);

        // Create extra entity
        ExtraEntity extra = new ExtraEntity();
        extra.setEffect("If this holomem is downed, you get life-2");
        extra = extraRepository.save(extra);

        // Create test card
        CardEntity card = new CardEntity();
        card.setCardid("HL-001");
        card.setCardset("Curious Universe");
        card.setCardtype(cardType);
        card.setCardcolour(cardColour);
        card.setHolomem("Ookami Mio");
        card.setBloomlvl("2nd");
        card.setHp(100);
        card.setRarity("SR");
        card.setImage("https://example.com/card.png");
        card.setExtra(extra);
        card.setBatonpass("2");
        cardRepository.save(card);

        // Create another test card
        CardEntity card2 = new CardEntity();
        card2.setCardid("HL-002");
        card2.setCardset("HoloX");
        card2.setCardtype(cardType);
        card2.setCardcolour(cardColour);
        card2.setHolomem("Suisei");
        card2.setBloomlvl("debut");
        card2.setHp(80);
        card2.setRarity("R");
        card2.setImage("https://example.com/card2.png");
        card2.setExtra(null);
        card2.setBatonpass("1");
        cardRepository.save(card2);
    }

    @Test
    void shouldGetAllCardsWithPagination() throws Exception {
        mockMvc.perform(get("/api/cards?page=0&size=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", greaterThan(0)))
                .andExpect(jsonPath("$.totalElements", equalTo(2)))
                .andExpect(jsonPath("$.totalPages", equalTo(1)))
                .andExpect(jsonPath("$.content[0].cardId", notNullValue()))
                .andExpect(jsonPath("$.content[0].holomem", notNullValue()));
    }

    @Test
    void shouldGetCardsWithPaginationDefaults() throws Exception {
        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", equalTo(2)))
                .andExpect(jsonPath("$.pageable.pageNumber", equalTo(0)))
                .andExpect(jsonPath("$.pageable.pageSize", equalTo(20)));
    }

    @Test
    void shouldSearchCardsByName() throws Exception {
        mockMvc.perform(get("/api/cards/search?cardName=HL-001"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSearchCardsByBloomLevel() throws Exception {
        mockMvc.perform(get("/api/cards/search?bloomLvl=2nd"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldSearchCardsByColour() throws Exception {
        mockMvc.perform(get("/api/cards/search?colour=blue&page=0&size=20"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSearchCardsByCardSet() throws Exception {
        mockMvc.perform(get("/api/cards/search?cardSet=HoloX"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSearchCardsByRarity() throws Exception {
        mockMvc.perform(get("/api/cards/search?rarity=SR"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSearchCardsWithMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/cards/search?holomem=Ookami%20Mio&rarity=SR&cardSet=Curious%20Universe"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnEmptyWhenNoCardsMatch() throws Exception {
        mockMvc.perform(get("/api/cards/search?holomem=NonexistentMember"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCardById() throws Exception {
        CardEntity card = cardRepository.findAll().get(0);

        mockMvc.perform(get("/api/cards/{id}", card.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo((int) card.getId())))
                .andExpect(jsonPath("$.cardId", notNullValue()))
                .andExpect(jsonPath("$.holomem", notNullValue()));
    }

    @Test
    void shouldReturn404WhenCardNotFound() throws Exception {
        mockMvc.perform(get("/api/cards/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCardWithAllFields() throws Exception {
        CardEntity card = cardRepository.findAll().get(0);

        mockMvc.perform(get("/api/cards/{id}", card.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.cardId", notNullValue()))
                .andExpect(jsonPath("$.cardSet", notNullValue()))
                .andExpect(jsonPath("$.cardTypeName", notNullValue()))
                .andExpect(jsonPath("$.cardColour", notNullValue()))
                .andExpect(jsonPath("$.bloomLvl", notNullValue()))
                .andExpect(jsonPath("$.hp", notNullValue()))
                .andExpect(jsonPath("$.rarity", notNullValue()))
                .andExpect(jsonPath("$.imageURL", notNullValue()));
    }
}
