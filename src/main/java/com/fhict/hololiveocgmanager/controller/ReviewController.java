package com.fhict.hololiveocgmanager.controller;

import com.fhict.hololiveocgmanager.dto.request.ReviewCreateRequest;
import com.fhict.hololiveocgmanager.dto.response.ReviewResponse;
import com.fhict.hololiveocgmanager.entity.UserEntity;
import com.fhict.hololiveocgmanager.exception.ForbiddenException;
import com.fhict.hololiveocgmanager.exception.NotFoundException;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import com.fhict.hololiveocgmanager.repository.ReviewRepository;
import com.fhict.hololiveocgmanager.repository.UserRepository;
import com.fhict.hololiveocgmanager.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = {"http://localhost:5173", "http://145.220.72.119"}, allowCredentials = "true")
public class ReviewController {
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserMapper userMapper;

    public ReviewController(UserRepository userRepository, ReviewService reviewService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.reviewService = reviewService;
        this.userMapper = userMapper;
    }

    @PostMapping("/{deckId}")
    public ReviewResponse createReview(@PathVariable Integer deckId, @RequestBody ReviewCreateRequest createRequest)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new ForbiddenException("User is not authenticated");
        }

        UserEntity user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new NotFoundException("Authenticated user not found in database"));

        return reviewService.createReview(userMapper.toDomain(user), deckId, createRequest);
    }

    @GetMapping("/{deckId}")
    public Page<ReviewResponse> getReviewsByDeck(@PathVariable Integer deckId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size)
    {
        Pageable pageable = PageRequest.of(page, size);
        return reviewService.getReviewsByDeckId(deckId, pageable);
    }
}