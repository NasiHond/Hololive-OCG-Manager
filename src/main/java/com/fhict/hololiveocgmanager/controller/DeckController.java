package com.fhict.hololiveocgmanager.controller;


import com.fhict.hololiveocgmanager.dto.request.CreateDeckRequest;
import com.fhict.hololiveocgmanager.dto.request.DeckCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.request.UpdateDeckRequest;
import com.fhict.hololiveocgmanager.dto.response.DeckCardResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckPageResponse;
import com.fhict.hololiveocgmanager.dto.response.DeckResponse;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.BadRequestException;
import com.fhict.hololiveocgmanager.exception.ForbiddenException;
import com.fhict.hololiveocgmanager.exception.NotFoundException;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.DeckService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decks")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class DeckController {
    private final DeckService deckService;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;

    public DeckController(DeckService deckService, UserRepository userRepository, DeckRepository deckRepository) {
        this.deckService = deckService;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
    }

    @PostMapping
    public DeckResponse createDeck(CreateDeckRequest createDeckRequest)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof String username)) {
            throw new ForbiddenException("User must be authenticated to create a deck");
        }
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found in database"));

        return deckService.createDeck(createDeckRequest, user.getId());
    }

    @GetMapping("{deckId}")
    public DeckPageResponse getDeckPage(@PathVariable Integer deckId)
    {
        DeckEntity deckEntity = deckRepository.findById(deckId)
                .orElseThrow(() -> new NotFoundException("Deck not found with id: " + deckId));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;

        if (currentUsername != null) {
            Integer currentUserId = userRepository.findByUsername(currentUsername)
                    .map(UserEntity::getId)
                    .orElse(null);

            if (deckEntity.getCreatorId().getId().equals(currentUserId) || deckEntity.getVisibility() == com.fhict.hololiveocgmanager.domain.Visibility.PUBLIC) {
                return deckService.getDeckPage(deckId);
            } else {
                throw new ForbiddenException("User can only access their own decks or public decks");
            }
        } else {
            if (deckEntity.getVisibility() == com.fhict.hololiveocgmanager.domain.Visibility.PUBLIC) {
                return deckService.getDeckPage(deckId);
            } else {
                throw new ForbiddenException("User must be authenticated to access private decks");
            }
        }
    }

    @GetMapping("/users/{userId}")
    public Page<DeckResponse> fetchDecksFromUser(@PathVariable Integer userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;

        if (currentUsername != null) {
            Integer currentUserId = userRepository.findByUsername(currentUsername)
                    .map(UserEntity::getId)
                    .orElse(null);

            if (userId.equals(currentUserId)) {
                return getDecksFromUser(userId, page, size);
            }
        }

        return getPublicDecksFromUser(userId, page, size);
    }

    @PutMapping("/{deckId}/cards")
    public DeckCardResponse updateDeckCard(@PathVariable Integer deckId, DeckCardUpdateRequest updateRequest)
    {
        if (updateRequest == null)
        {
            throw new BadRequestException("Update request body is required");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;

        if (currentUsername == null) {
            throw new ForbiddenException("User must be authenticated to update a deck");
        }
        else {
            Integer currentUserId = userRepository.findByUsername(currentUsername)
                    .map(UserEntity::getId)
                    .orElse(null);

            if (!deckRepository.findById(deckId)
                    .map(deck -> deck.getCreatorId().getId().equals(currentUserId))
                    .orElse(false)){
                throw new ForbiddenException("User can only update their own decks");
            }
            else {
                return deckService.updateDeckCard(deckId, updateRequest);
            }
        }
    }

    @PutMapping("/{deckId}")
    DeckResponse updateDeckFromUser(@PathVariable Integer deckId, @RequestBody UpdateDeckRequest updateDeckRequest)
    {
        if (updateDeckRequest == null)
        {
            throw new BadRequestException("Update request body is required");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;

        if (currentUsername == null) {
            throw new ForbiddenException("User must be authenticated to update a deck");
        }
        else {
            Integer currentUserId = userRepository.findByUsername(currentUsername)
                    .map(UserEntity::getId)
                    .orElse(null);

            if (!deckRepository.findById(deckId)
                    .map(deck -> deck.getCreatorId().getId().equals(currentUserId))
                    .orElse(false)){
                throw new ForbiddenException("User can only update their own decks");
            }
            else {
                return deckService.updateDeckByUserId(deckId, updateDeckRequest);
            }
        }
    }

    private Page<DeckResponse> getDecksFromUser(Integer userId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);

        return deckService.getDecksByUser(userId, pageable);
    }

    private Page<DeckResponse> getPublicDecksFromUser(Integer userId, int page, int size)
    {
        Pageable pageable = PageRequest.of(page, size);

        return deckService.getPublicDecksByUser(userId, pageable);
    }
}