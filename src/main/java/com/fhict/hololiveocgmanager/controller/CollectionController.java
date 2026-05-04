package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.response.CollectionCardsPageResponse;
import com.fhict.hololiveocgmanager.service.CollectionService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CollectionController {
    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
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

        Integer parsedUserId;
        try {
            parsedUserId = Integer.valueOf(userId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId must be a number");
        }

        return collectionService.getCollectionByUserId(parsedUserId, page, size);
    }
}
