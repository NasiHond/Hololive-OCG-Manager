package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.CollectionCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.service.CollectionService;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CollectionController {
    private final CollectionService collectionService;
    private final UserRepository userRepository;

    public CollectionController(CollectionService collectionService, UserRepository userRepository) {
        this.collectionService = collectionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public CollectionCardsPageResponse getCollection(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId path variable is required");
        }

        if (userId.contains("${") || userId.contains("}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId format");
        }

        int parsedUserId;
        try {
            parsedUserId = Integer.parseInt(userId);
        } catch (NumberFormatException _) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be a number");
        }

        return collectionService.getCollectionByUserId(parsedUserId, page, size);
    }

    @GetMapping("/{userId}/{cardId}")
    public CollectionCardResponse getCollectionCard(
            @PathVariable String userId,
            @PathVariable String cardId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId path variable is required");
        }

        if (cardId == null || cardId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cardId path variable is required");
        }

        if (userId.contains("${") || userId.contains("}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId format");
        }

        if (cardId.contains("${") || cardId.contains("}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid cardId format");
        }

        int parsedUserId;
        try {
            parsedUserId = Integer.parseInt(userId);
        } catch (NumberFormatException _) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be a number");
        }

        int parsedCardId;
        try {
            parsedCardId = Integer.parseInt(cardId);
        } catch (NumberFormatException _) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cardId must be a number");
        }

        return collectionService.getCollectionCardByUserIdAndCardId(parsedUserId, parsedCardId);
    }

    @PutMapping("/{userId}/cards")
    public ResponseEntity<CollectionCardResponse> updateCollectionCard(
            @PathVariable String userId,
            @RequestBody CollectionCardUpdateRequest updateRequest
    ){
        if (updateRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CollectionCardUpdateRequest body is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof String)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authenticated");
        }

        String username = (String) authentication.getPrincipal();
        Integer authenticatedUserId = userRepository.findByUsername(username)
                .map(UserEntity::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized"));

        CollectionCardResponse updated = collectionService.updateCollectionCardByUserId(
                authenticatedUserId,
                updateRequest.getCollectionId(),
                updateRequest.getCardId(),
                updateRequest.getAmount());
        return ResponseEntity.ok(updated);
    }
}
