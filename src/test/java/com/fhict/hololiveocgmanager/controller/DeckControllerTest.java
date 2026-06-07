package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.dto.request.CreateDeckRequest;
import com.fhict.hololiveocgmanager.dto.request.DeckCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.request.UpdateDeckRequest;
import com.fhict.hololiveocgmanager.dto.response.DeckCardResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckPageResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.GlobalExceptionHandler;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.DeckService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DeckControllerTest {

    @Mock
    private DeckService deckService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeckRepository deckRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new DeckController(deckService, userRepository, deckRepository))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createDeckRejectsUnauthenticated() throws Exception {
        CreateDeckRequest request = new CreateDeckRequest();
        request.setTitle("New Deck");
        request.setVisibility(Visibility.PUBLIC);

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Deck")
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User must be authenticated to create a deck"));
    }

    @Test
    void createDeckRejectsMissingUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Deck")
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Authenticated user not found in database"));
    }

    @Test
    void createDeckReturnsResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(5).username("user1").build()));

        DeckResponse response = DeckResponse.builder()
                .id(10)
                .title("New Deck")
                .ownerId(5)
                .visibility(Visibility.PUBLIC)
                .build();

        UserEntity user = userRepository.findAll().getFirst();

        when(deckService.createDeck(any(CreateDeckRequest.class), user)).thenReturn(response);

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "New Deck")
                        .param("visibility", "PUBLIC"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New Deck"))
                .andExpect(jsonPath("$.ownerId").value(5))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    @Test
    void getDecksReturnsVisibleDecksForAnonymous() throws Exception {
        when(deckService.getVisibleDecks(eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(DeckResponse.builder().id(1).title("Public").build()),
                        PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Public"));
    }

    @Test
    void getDeckCardsByCardIdRejectsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/decks/cards/5"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User must be authenticated to access deck cards"));
    }

    @Test
    void getDeckCardsByCardIdReturnsCards() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(4).build()));

        DeckCardResponse cardResponse = DeckCardResponse.builder()
                .deckId(2)
                .cardID("H-001")
                .count(1)
                .build();

        when(deckService.getDeckCardsByCardIdAndUserId(5, 4)).thenReturn(List.of(cardResponse));

        mockMvc.perform(get("/api/decks/cards/5"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].deckId").value(2))
                .andExpect(jsonPath("$[0].cardID").value("H-001"))
                .andExpect(jsonPath("$[0].count").value(1));
    }

    @Test
    void getDeckPageRejectsMissingDeck() throws Exception {
        when(deckRepository.findById(10)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/decks/10"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Deck not found with id: 10"));
    }

    @Test
    void getDeckPageReturnsForOwner() throws Exception {
        DeckEntity deckEntity = DeckEntity.builder()
                .id(1)
                .creatorId(UserEntity.builder().id(9).build())
                .visibility(Visibility.PRIVATE)
                .build();

        when(deckRepository.findById(1)).thenReturn(Optional.of(deckEntity));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(UserEntity.builder().id(9).build()));
        when(deckService.getDeckPage(1)).thenReturn(DeckPageResponse.builder()
                .deck(DeckResponse.builder().id(1).title("Mine").build())
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        mockMvc.perform(get("/api/decks/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deck.id").value(1))
                .andExpect(jsonPath("$.deck.title").value("Mine"));
    }

    @Test
    void getDeckPageRejectsPrivateForAnonymous() throws Exception {
        DeckEntity deckEntity = DeckEntity.builder()
                .id(1)
                .creatorId(UserEntity.builder().id(9).build())
                .visibility(Visibility.PRIVATE)
                .build();

        when(deckRepository.findById(1)).thenReturn(Optional.of(deckEntity));

        mockMvc.perform(get("/api/decks/1"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User must be authenticated to access private decks"));
    }

    @Test
    void fetchDecksFromUserReturnsOwnDecks() throws Exception {
        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(7).build()));
        when(deckService.getDecksByUser(eq(7), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(DeckResponse.builder().id(1).title("Mine").build()),
                        PageRequest.of(0, 20), 1));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        mockMvc.perform(get("/api/decks/users/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Mine"));
    }

    @Test
    void fetchDecksFromUserReturnsPublicDecksForOtherUser() throws Exception {
        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(7).build()));
        when(deckService.getPublicDecksByUser(eq(8), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(DeckResponse.builder().id(2).title("Public").build()),
                        PageRequest.of(0, 20), 1));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        mockMvc.perform(get("/api/decks/users/8"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Public"));
    }

    @Test
    void updateDeckCardRejectsNullBody() throws Exception {
        mockMvc.perform(put("/api/decks/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void updateDeckCardRejectsUnauthenticated() throws Exception {
        DeckCardUpdateRequest request = DeckCardUpdateRequest.builder()
                .cardId(1)
                .count(1)
                .build();

        mockMvc.perform(put("/api/decks/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User must be authenticated to update a deck"));
    }

    @Test
    void updateDeckCardRejectsNonOwner() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(5).build()));
        when(deckRepository.findById(1))
                .thenReturn(Optional.of(DeckEntity.builder().id(1).creatorId(UserEntity.builder().id(7).build()).build()));

        DeckCardUpdateRequest request = DeckCardUpdateRequest.builder()
                .cardId(1)
                .count(1)
                .build();

        mockMvc.perform(put("/api/decks/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User can only update their own decks"));
    }

    @Test
    void updateDeckCardReturnsResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(5).build()));
        when(deckRepository.findById(1))
                .thenReturn(Optional.of(DeckEntity.builder().id(1).creatorId(UserEntity.builder().id(5).build()).build()));

        DeckCardResponse response = DeckCardResponse.builder()
                .deckId(1)
                .cardID("H-002")
                .count(2)
                .build();

        when(deckService.updateDeckCard(eq(1), any(DeckCardUpdateRequest.class))).thenReturn(response);

        DeckCardUpdateRequest request = DeckCardUpdateRequest.builder()
                .cardId(2)
                .count(2)
                .build();

        mockMvc.perform(put("/api/decks/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.deckId").value(1))
                .andExpect(jsonPath("$.cardID").value("H-002"))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void updateDeckFromUserRejectsNullBody() throws Exception {
        mockMvc.perform(put("/api/decks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void updateDeckFromUserRejectsUnauthenticated() throws Exception {
        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setTitle("Updated");

        mockMvc.perform(put("/api/decks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User must be authenticated to update a deck"));
    }

    @Test
    void updateDeckFromUserReturnsResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(5).build()));
        when(deckRepository.findById(1))
                .thenReturn(Optional.of(DeckEntity.builder().id(1).creatorId(UserEntity.builder().id(5).build()).build()));

        DeckResponse response = DeckResponse.builder()
                .id(1)
                .title("Updated")
                .build();

        when(deckService.updateDeckByUserId(eq(1), any(UpdateDeckRequest.class))).thenReturn(response);

        UpdateDeckRequest request = new UpdateDeckRequest();
        request.setTitle("Updated");

        mockMvc.perform(put("/api/decks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated"));
    }
}

