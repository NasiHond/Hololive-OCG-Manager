package com.fhict.hololiveocgmanager.integration;

import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.entity.*;
import com.fhict.hololiveocgmanager.repository.*;
import com.fhict.hololiveocgmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class CollectionControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    JwtService jwtService;

    private MockMvc mockMvc;

    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private CollectionCardsRepository collectionCardsRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardTypeRepository cardTypeRepository;
    @Autowired
    private ColourRepository colourRepository;
    @Autowired
    private ExtraRepository extraRepository;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        collectionCardsRepository.deleteAll();
        cardRepository.deleteAll();
        cardTypeRepository.deleteAll();
        colourRepository.deleteAll();
        extraRepository.deleteAll();
        userRepository.deleteAll();
        seedTestData();
    }

    private void seedTestData()
    {
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setEmail("test@mail.com");
        user.setPasswordHash("testPassword");
        userRepository.save(user);

        CollectionEntity collection =  new CollectionEntity();
        collection.setVisibility(Visibility.PRIVATE);
        collection.setOwnerId(user);
        collectionRepository.save(collection);

        seedCardData();

        CollectionCardsEntity collectionCards = new CollectionCardsEntity();
        collectionCards.setCardId(cardRepository.findAll().getFirst());
        collectionCards.setCardCount(4);
        collectionCards.setCollectionId(collection);
        collectionCardsRepository.save(collectionCards);

        CollectionCardsEntity collectionCards2 = new CollectionCardsEntity();
        collectionCards2.setCardId(cardRepository.findAll().get(1));
        collectionCards2.setCardCount(3);
        collectionCards2.setCollectionId(collection);
        collectionCardsRepository.save(collectionCards2);
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

        // Create third test card
        CardEntity card3 = new CardEntity();
        card3.setCardid("Bp2-01");
        card3.setCardset("Blooming Radiance");
        card3.setCardtype(cardType);
        card3.setCardcolour(cardColour);
        card3.setHolomem("Reine");
        card3.setBloomlvl("2nd");
        card3.setHp(190);
        card3.setRarity("UR");
        card3.setImage("https://example.com/card3.png");
        card3.setExtra(null);
        card3.setBatonpass("2");
        cardRepository.save(card3);
    }

    @Test
    void shouldReturnCollectionWithGetCollection() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        CollectionEntity collectionEntity = collectionRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(get("/api/collections/{userId}", user.getId())
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(collectionEntity.getId()))
                .andExpect(jsonPath("$.owner.id").value(collectionEntity.getOwnerId().getId()))
                .andExpect(jsonPath("$.totalCards").value(2))
                .andExpect(jsonPath("$.totalCount").value(7))
                .andExpect(jsonPath("$.visibility").value(collectionEntity.getVisibility().toString()));
    }

    @Test
    void shouldReturnCollectionCardsWithGetCollectionCards() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        CollectionEntity collectionEntity = collectionRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(get("/api/collections/{userId}/cards", user.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].cardId").value("HL-001"))
                .andExpect(jsonPath("$.content[0].cardCount").value(4))
                .andExpect(jsonPath("$.content[0].collection.id").value(collectionEntity.getId()))
                .andExpect(jsonPath("$.content[1].cardId").value("HL-002"))
                .andExpect(jsonPath("$.content[1].cardCount").value(3))
                .andExpect(jsonPath("$.content[1].collection.id").value(collectionEntity.getId()));
    }

    @Test
    void shouldReturnForbiddenWithGetCollectionWhenUnauthorized() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        mockMvc.perform(get("/api/collections/{userId}", user.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnForbiddenWithGetCollectionCardsWhenUnauthorized() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        mockMvc.perform(get("/api/collections/{userId}/cards", user.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnCollectionCardWithGetCollectionCard() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        CardEntity card = cardRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(get("/api/collections/{userId}/{cardId}", user.getId(), card.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value("HL-001"))
                .andExpect(jsonPath("$.cardCount").value(4));
    }

     @Test
    void shouldReturnForbiddenWithGetCollectionCardWhenUnauthorized() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        CardEntity card = cardRepository.findAll().getFirst();
        mockMvc.perform(get("/api/collections/{userId}/{cardId}", user.getId(), card.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWithGetCollectionCardWhenNotFound() throws Exception
    {
        UserEntity user = userRepository.findAll().getFirst();
        CardEntity card = cardRepository.findAll().get(2);
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(get("/api/collections/{userId}/{cardId}", user.getId(), card.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnCollectionCardWithUpdateCollectionCard() throws Exception
    {
        CollectionEntity collection = collectionRepository.findAll().getFirst();
        UserEntity user = userRepository.findAll().getFirst();
        CardEntity card = cardRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(put("/api/collections/{userId}/cards", user.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(String.format("""
                        {
                            "collectionId": %d,
                            "cardId": %d,
                            "count": 5
                        }
                        """, collection.getId(), card.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value("HL-001"))
                .andExpect(jsonPath("$.cardCount").value(5))
                .andExpect(jsonPath("$.collection.id").value(collection.getId()));
    }

    @Test
    void shouldReturnCollectionCardWithUpdateCollectionCardWhileCardIsNotInCollection() throws Exception
    {
        CollectionEntity collection = collectionRepository.findAll().getFirst();
        UserEntity user = userRepository.findAll().getFirst();
        CardEntity card = cardRepository.findAll().get(2);
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(put("/api/collections/{userId}/cards", user.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content(String.format("""
                        {
                            "collectionId": %d,
                            "cardId": %d,
                            "count": 2
                        }
                        """, collection.getId(), card.getId())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value("Bp2-01"))
                .andExpect(jsonPath("$.cardCount").value(2))
                .andExpect(jsonPath("$.collection.totalCards").value(3))
                .andExpect(jsonPath("$.collection.id").value(collection.getId()));
    }
}
