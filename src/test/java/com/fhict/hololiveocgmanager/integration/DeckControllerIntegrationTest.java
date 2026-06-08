package com.fhict.hololiveocgmanager.integration;

import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.repository.*;
import com.fhict.hololiveocgmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class DeckControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    JwtService jwtService;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardTypeRepository cardTypeRepository;
    @Autowired
    private ColourRepository colourRepository;
    @Autowired
    private ExtraRepository extraRepository;
    @Autowired
    private DeckCardsRepository deckCardsRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
        deckRepository.deleteAll();
        cardRepository.deleteAll();
        cardTypeRepository.deleteAll();
        colourRepository.deleteAll();
        extraRepository.deleteAll();
        deckCardsRepository.deleteAll();
        seedTestData();
    }

    private void seedTestData() {
        UserEntity user1 = new UserEntity();
        user1.setUsername("testuser1");
        user1.setEmail("aMail@mail.com");
        user1.setPasswordHash("password");
        userRepository.save(user1);

        seedCardData();

        DeckEntity deck1 = new DeckEntity();
        deck1.setDeckName("Test Deck 1");
        deck1.setDeckDescription("This is a test deck");
        deck1.setVisibility(Visibility.PUBLIC);
        deck1.setCreatorId(user1);
        deckRepository.save(deck1);

        DeckEntity deck2 = new DeckEntity();
        deck2.setDeckName("Private Test Deck 2");
        deck2.setDeckDescription("This is a private test deck");
        deck2.setVisibility(Visibility.PRIVATE);
        deck2.setCreatorId(user1);
        deckRepository.save(deck2);

        CardEntity card1 = cardRepository.findAll().getFirst();
        CardEntity card2 = cardRepository.findAll().get(1);

        DeckCardsEntity deckCard1 = new DeckCardsEntity();
        deckCard1.setCardId(card1);
        deckCard1.setDeckId(deck1);
        deckCard1.setCardCount(25);
        deckCardsRepository.save(deckCard1);

        DeckCardsEntity deckCard2 = new DeckCardsEntity();
        deckCard2.setCardId(card2);
        deckCard2.setDeckId(deck1);
        deckCard2.setCardCount(25);
        deckCardsRepository.save(deckCard2);
    }

    private void seedCardData() {
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
    void shouldReturnDeckWithCreateDeck() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "title": "New Test Deck",
                                    "description": "This is a new test deck",
                                    "visibility": "PUBLIC"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Test Deck"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateDeckWithNoTitle() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(post("/api/decks")
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "title": "",
                                    "description": "This is a new test deck",
                                    "visibility": "PUBLIC"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnPublicDecksWhenGetDecksWhileUnauthorized() throws Exception {
        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].visibility").value("PUBLIC"));
    }

    @Test
    void shouldReturnPublicAndPersonalDecksWhenGetDecksWhileAuthorized() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(get("/api/decks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.content[1].visibility").value("PRIVATE"));
    }

    @Test
    void shouldReturnListOfDeckCardsWhenGetDeckCardsByCardId() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        DeckEntity deck = deckRepository.findAll().getFirst();
        CardEntity card = cardRepository.findAll().getFirst();
        mockMvc.perform(get("/api/decks/cards/{cardId}", card.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cardID").value(card.getCardid()))
                .andExpect(jsonPath("$[0].deckId").value(deck.getId()));
    }

    @Test
    void shouldReturnForbiddenWhenGetDeckCardsByCardIdWhileUnauthenticated() throws Exception {
        CardEntity card = cardRepository.findAll().getFirst();
        mockMvc.perform(get("/api/decks/cards/{cardId}", card.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnDeckPageWhenGetDeckPage() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        DeckEntity deck = deckRepository.findAll().getFirst();
        mockMvc.perform(get("/api/decks/{deckId}", deck.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deck.title").value("Test Deck 1"))
                .andExpect(jsonPath("$.deck.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.cards.length()").value(2));
    }

    @Test
    void shouldReturnNotFoundWhenGetDeckPageWhileUnauthorized() throws Exception {
        mockMvc.perform(get("/api/decks/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllPersonalDecksWhenFetchDecksFromUserWhileAuthorized() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(get("/api/decks/users/{userId}", user.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldReturnAllPublicDecksWhenFetchDecksFromUserWhileUnauthorized() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        mockMvc.perform(get("/api/decks/users/{userId}", user.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].visibility").value("PUBLIC"));
    }

    @Test
    void shouldReturnDeckCardWhenUpdateDeckCardWhileAuthorized() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        DeckEntity deck = deckRepository.findAll().getFirst();
        DeckCardsEntity deckCardsEntity = deckCardsRepository.findAll().getFirst();
        mockMvc.perform(put("/api/decks/{deckId}/cards", deck.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(String.format("""
                        {
                            "cardId": %d,
                            "count": 30
                        }
                        """, deckCardsEntity.getCardId().getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deckId").value(deck.getId()))
                .andExpect(jsonPath("$.id").value(deckCardsEntity.getCardId().getId()))
                .andExpect(jsonPath("$.count").value(30));
    }

    @Test
    void shouldReturnForbiddenWhenUpdateDeckCardWhileUnauthorized() throws Exception {
        DeckEntity deck = deckRepository.findAll().getFirst();
        mockMvc.perform(put("/api/decks/{deckId}/cards",  deck.getId())
                        .contentType("application/json")
                        .content("""
                                {
                                    "cardId": 1,
                                    "cardCount": 30
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateDeckCardWithMissingParameter() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        DeckEntity deck = deckRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(put("/api/decks/{deckId}/cards", deck.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "cardCount": 30
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnDeckWhenUpdateDeckWhileAuthorized() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        DeckEntity deck = deckRepository.findAll().getFirst();
        mockMvc.perform(put("/api/decks/{deckId}", deck.getId())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                            "title": "Updated Test Deck",
                            "description": "This is an updated test deck",
                            "visibility": "PRIVATE"
                        }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Test Deck"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    @Test
    void shouldReturnBadRequestWhenUpdateDeckWithMissingParameter() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        DeckEntity deck = deckRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(put("/api/decks/{deckId}", deck.getId())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                            "description": "This is an updated test deck",
                            "visibility": "PRIVATE"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnForbiddenWhenUpdateDeckWhileUnauthorized() throws Exception {
        DeckEntity deck = deckRepository.findAll().getFirst();
        mockMvc.perform(put("/api/decks/{deckId}", deck.getId())
                .contentType("application/json")
                .content("""
                        {
                            "title": "Updated Test Deck",
                            "description": "This is an updated test deck",
                            "visibility": "PRIVATE"
                        }
                        """))
                .andExpect(status().isForbidden());

        UserEntity user2 = new UserEntity();
        user2.setUsername("testuser2");
        user2.setEmail("test2@mail.com");
        user2.setPasswordHash("apassword");
        userRepository.save(user2);

        String token = jwtService.generateAccessToken(user2.getUsername());
        mockMvc.perform(put("/api/decks/{deckId}", deck.getId())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("""
                        {
                            "title": "Updated Test Deck",
                            "description": "This is an updated test deck",
                            "visibility": "PRIVATE"
                        }
                        """))
                .andExpect(status().isForbidden());
    }
}
