package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.CollectionCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.domain.Visibility;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.GlobalExceptionHandler;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.CollectionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CollectionControllerTest {

    @Mock
    private CollectionService collectionService;

    @Mock
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new CollectionController(collectionService, userRepository))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCollectionRejectsBlankUserId() throws Exception {
        mockMvc.perform(get("/api/collections/{userId}", " "))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("userId path variable is required"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:bad-request"));
    }

    @Test
    void getCollectionRejectsTemplateInjection() throws Exception {
        mockMvc.perform(get("/api/collections/{userId}", "${bad}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Invalid userId format"));
    }

    @Test
    void getCollectionRejectsNonNumericUserId() throws Exception {
        mockMvc.perform(get("/api/collections/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("userId must be a number"));
    }

    @Test
    void getCollectionReturnsPage() throws Exception {
        CollectionCardsPageResponse response = CollectionCardsPageResponse.builder()
                .collection(CollectionResponse.builder()
                        .id(10)
                        .ownerId(3)
                        .visibility(Visibility.PUBLIC)
                        .totalCards(2)
                        .totalCount(5)
                        .build())
                .cards(List.of(CollectionCardResponse.builder()
                        .cardId("H-001")
                        .cardCount(2)
                        .name("Ina")
                        .build()))
                .page(0)
                .size(20)
                .last(true)
                .totalElements(1)
                .hasMore(false)
                .totalPages(1)
                .build();

        when(collectionService.getCollectionByUserId(3, 0, 20)).thenReturn(response);

        mockMvc.perform(get("/api/collections/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.collection.id").value(10))
                .andExpect(jsonPath("$.collection.ownerId").value(3))
                .andExpect(jsonPath("$.collection.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.cards[0].cardId").value("H-001"))
                .andExpect(jsonPath("$.cards[0].cardCount").value(2));
    }

    @Test
    void getCollectionCardRejectsNonNumericCardId() throws Exception {
        mockMvc.perform(get("/api/collections/1/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("cardId must be a number"));
    }

    @Test
    void updateCollectionCardRejectsNullBody() throws Exception {
        mockMvc.perform(put("/api/collections/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Malformed request body"));
    }

    @Test
    void updateCollectionCardRejectsUnauthenticated() throws Exception {
        CollectionCardUpdateRequest request = CollectionCardUpdateRequest.builder()
                .collectionId(1)
                .cardId(2)
                .amount(1)
                .build();

        mockMvc.perform(put("/api/collections/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User is not authenticated"));
    }

    @Test
    void updateCollectionCardRejectsUnauthorizedUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        CollectionCardUpdateRequest request = CollectionCardUpdateRequest.builder()
                .collectionId(1)
                .cardId(2)
                .amount(1)
                .build();

        mockMvc.perform(put("/api/collections/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("User is not authorized"));
    }

    @Test
    void updateCollectionCardReturnsResponse() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("user1", "N/A", List.of()));

        when(userRepository.findByUsername("user1"))
                .thenReturn(Optional.of(UserEntity.builder().id(5).username("user1").build()));

        CollectionCardResponse response = CollectionCardResponse.builder()
                .cardId("H-002")
                .cardCount(3)
                .build();

        when(collectionService.updateCollectionCardByUserId(5, 1, 2, 3))
                .thenReturn(response);

        CollectionCardUpdateRequest request = CollectionCardUpdateRequest.builder()
                .collectionId(1)
                .cardId(2)
                .amount(3)
                .build();

        mockMvc.perform(put("/api/collections/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value("H-002"))
                .andExpect(jsonPath("$.cardCount").value(3));
    }
}




