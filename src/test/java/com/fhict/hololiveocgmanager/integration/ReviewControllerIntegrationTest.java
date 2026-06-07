package com.fhict.hololiveocgmanager.integration;

import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.ReviewEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import com.fhict.hololiveocgmanager.repository.ReviewRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
class ReviewControllerIntegrationTest {

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
    private ReviewRepository reviewRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        reviewRepository.deleteAll();
        deckRepository.deleteAll();
        userRepository.deleteAll();
        seedTestData();
    }

    private void seedTestData() {
        UserEntity user = new UserEntity();
        user.setUsername("testUser");
        user.setEmail("test@mail.com");
        user.setPasswordHash("testPassword");
        userRepository.save(user);

        DeckEntity deck1 = new DeckEntity();
        deck1.setDeckName("Test Deck 1");
        deck1.setDeckDescription("This is a test deck");
        deck1.setVisibility(Visibility.PUBLIC);
        deck1.setCreatorId(user);
        deckRepository.save(deck1);

        ReviewEntity review = new ReviewEntity();
        review.setDeck(deck1);
        review.setUser(user);
        review.setRating(4.0);
        review.setComment("Great deck!");
        reviewRepository.save(review);
    }

    @Test
    void shouldReturnReviewWhenCreateReview() throws Exception {
        UserEntity user = userRepository.findAll().getFirst();
        DeckEntity deck = deckRepository.findAll().getFirst();
        String token = jwtService.generateAccessToken(user.getUsername());
        mockMvc.perform(post("/api/reviews/{deckId}", deck.getId())
                        .header("Authorization", "Bearer " + token)
                        .contentType("application/json")
                        .content("""
                                {
                                    "rating": 5,
                                    "comment": "test comment"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("test comment"));
    }

    @Test
    void shouldReturnForbiddenWhenCreateReviewWhileUnauthenticated() throws Exception {
        DeckEntity deck = deckRepository.findAll().getFirst();
        mockMvc.perform(post("/api/reviews/{deckId}", deck.getId())
                        .contentType("application/json")
                        .content("""
                                {
                                    "rating": 5,
                                    "comment": "test comment"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnReviewsWhenGetReviewsByDeck() throws Exception {
        DeckEntity deck = deckRepository.findAll().getFirst();
        mockMvc.perform(get("/api/reviews/{deckId}", deck.getId())
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].rating").value(4.0))
                .andExpect(jsonPath("$.content[0].comment").value("Great deck!"));
    }
}
