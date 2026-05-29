package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.domain.Art;
import com.fhict.hololiveocgmanager.domain.Card;
import com.fhict.hololiveocgmanager.domain.Keyword;
import com.fhict.hololiveocgmanager.domain.Tag;
import com.fhict.hololiveocgmanager.entity.ArtcostEntity;
import com.fhict.hololiveocgmanager.entity.CardEntity;
import com.fhict.hololiveocgmanager.entity.ColourEntity;
import com.fhict.hololiveocgmanager.exception.GlobalExceptionHandler;
import com.fhict.hololiveocgmanager.mapper.CardMapper;
import com.fhict.hololiveocgmanager.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new CardController(cardRepository, cardMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getAllCardsReturnsPage() throws Exception {
        CardEntity entity = new CardEntity();
        entity.setId(1);

        Card domain = sampleCardDomain();

        when(cardRepository.findAll(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(cardMapper.toDomain(entity)).thenReturn(domain);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(101))
                .andExpect(jsonPath("$.content[0].cardId").value("H-001"))
                .andExpect(jsonPath("$.content[0].cardTypeName").value("Support"))
                .andExpect(jsonPath("$.content[0].cardColour").value("Blue"))
                .andExpect(jsonPath("$.content[0].keywords[0].name").value("Bloom"))
                .andExpect(jsonPath("$.content[0].tags[0].name").value("Idol"))
                .andExpect(jsonPath("$.content[0].arts[0].costs[0].colourName").value("Blue"));
    }

    @Test
    void searchCardsUsesFilters() throws Exception {
        CardEntity entity = new CardEntity();
        entity.setId(2);
        Card domain = sampleCardDomain();

        when(cardRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 20), 1));
        when(cardMapper.toDomain(entity)).thenReturn(domain);

        mockMvc.perform(get("/api/cards/search")
                        .param("cardName", "Ina")
                        .param("parallel", "  "))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].cardId").value("H-001"));
    }

    @Test
    void getCardByIdReturnsCard() throws Exception {
        CardEntity entity = new CardEntity();
        entity.setId(10);

        when(cardRepository.findById(10)).thenReturn(Optional.of(entity));
        when(cardMapper.toDomain(entity)).thenReturn(sampleCardDomain());

        mockMvc.perform(get("/api/cards/10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardId").value("H-001"));
    }

    @Test
    void getCardByIdReturnsServerErrorWhenMissing() throws Exception {
        when(cardRepository.findById(anyInt())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/cards/999"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.detail").value("Unexpected error"))
                .andExpect(jsonPath("$.type").value("urn:problem-type:internal-server-error"));
    }

    private Card sampleCardDomain() {
        ColourEntity costColour = new ColourEntity();
        costColour.setColour("Blue");
        costColour.setImageUrl("/blue.png");

        ArtcostEntity cost = new ArtcostEntity();
        cost.setId(1);
        cost.setAmount(2);
        cost.setColour(costColour);

        Art art = Art.builder()
                .id(77)
                .name("Strike")
                .effect("Deal damage")
                .damage(30)
                .critColourName("Blue")
                .costs(List.of(cost))
                .build();

        return Card.builder()
                .id(101)
                .cardID("H-001")
                .cardset("Set-A")
                .cardTypeID(5)
                .cardTypeName("Support")
                .cardColour("Blue")
                .batonpass("BP")
                .holomem("Ina")
                .bloomLvl("1")
                .hp(100)
                .rarity("R")
                .imageURL("/image.png")
                .extraEffect("Extra")
                .keywords(List.of(Keyword.builder().ID(1).type("Skill").name("Bloom").effect("Grow").build()))
                .tags(List.of(Tag.builder().id(3).name("Idol").build()))
                .arts(List.of(art))
                .build();
    }
}


