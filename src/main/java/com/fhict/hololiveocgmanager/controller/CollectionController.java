package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.CollectionCardUpdateRequest;
import com.fhict.hololiveocgmanager.dto.response.CollectionCardResponse;
import com.fhict.hololiveocgmanager.dto.response.CollectionResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.service.CollectionService;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fhict.hololiveocgmanager.exception.BadRequestException;
import com.fhict.hololiveocgmanager.exception.ForbiddenException;

import java.util.Optional;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "{app.cors.allowed-origins}", allowCredentials = "true")
public class CollectionController {
    private final CollectionService collectionService;
    private final UserRepository userRepository;

    public CollectionController(CollectionService collectionService, UserRepository userRepository) {
        this.collectionService = collectionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{userId}")
    public CollectionResponse getCollection(@PathVariable Integer userId)
    {
        Optional<UserEntity> currentUser = Optional.empty();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            currentUser = userRepository.findByUsername(authentication.getName());
        }
        return collectionService.getCollectionByUserId(userId, currentUser);
    }

    @GetMapping("/{userId}/cards")
    public Page<CollectionCardResponse> getCollectionCards(@PathVariable Integer userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Optional<UserEntity> currentUser = Optional.empty();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            currentUser = userRepository.findByUsername(authentication.getName());
        }
        return collectionService.getCollectionCards(userId, currentUser, pageable);
    }

    @GetMapping("/{userId}/{cardId}")
    public CollectionCardResponse getCollectionCard(
            @PathVariable Integer userId,
            @PathVariable Integer cardId) {
        if (userId == null || cardId == null) {
            throw new BadRequestException("User ID and Card ID are required");
        }

        Optional<UserEntity> currentUser = Optional.empty();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            currentUser = userRepository.findByUsername(authentication.getName());
        }

        return collectionService.getCollectionCardByUserIdAndCardId(userId, cardId, currentUser);
    }

    @PutMapping("/{userId}/cards")
    public ResponseEntity<CollectionCardResponse> updateCollectionCard(
            @PathVariable Integer userId,
            @RequestBody CollectionCardUpdateRequest updateRequest
    ) {
        if (updateRequest == null) {
            throw new BadRequestException("CollectionCardUpdateRequest body is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ForbiddenException("User is not authenticated");
        }

        UserEntity currentUser = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new ForbiddenException("User not found"));

        if (!currentUser.getId().equals(userId)) {
            throw new ForbiddenException("User is not allowed to modify this resource");
        }

        CollectionCardResponse updated =
                collectionService.updateCollectionCardByUserId(
                        userId,
                        updateRequest.getCollectionId(),
                        updateRequest.getCardId(),
                        updateRequest.getCount()
                );

        return ResponseEntity.ok(updated);
    }
}
