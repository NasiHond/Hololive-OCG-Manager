package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.Review;
import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.dto.request.ReviewCreateRequest;
import com.fhict.hololiveocgmanager.dto.request.ReviewUpdateRequest;
import com.fhict.hololiveocgmanager.dto.response.ReviewResponse;
import com.fhict.hololiveocgmanager.entity.DeckEntity;
import com.fhict.hololiveocgmanager.entity.ReviewEntity;
import com.fhict.hololiveocgmanager.exception.NotFoundException;
import com.fhict.hololiveocgmanager.mapper.DeckMapper;
import com.fhict.hololiveocgmanager.mapper.ReviewMapper;
import com.fhict.hololiveocgmanager.mapper.UserMapper;
import com.fhict.hololiveocgmanager.repository.DeckRepository;
import com.fhict.hololiveocgmanager.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final DeckRepository deckRepository;
    private final ReviewMapper reviewMapper;
    private final DeckMapper deckMapper;
    private final UserMapper userMapper;

    public ReviewServiceImpl(ReviewRepository reviewRepository, DeckRepository deckRepository, ReviewMapper reviewMapper, DeckMapper deckMapper, UserMapper userMapper) {
        this.reviewRepository = reviewRepository;
        this.deckRepository = deckRepository;
        this.reviewMapper = reviewMapper;
        this.deckMapper = deckMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Page<ReviewResponse> getReviewsByDeckId(Integer deckId, Pageable pageable)
    {
        return reviewRepository.getAllByDeckId(deckId, pageable)
                .map(reviewMapper::toDomain)
                .map(reviewMapper::toResponse);
    }

    @Override
    public ReviewResponse createReview(User user, Integer deckId, ReviewCreateRequest reviewCreateRequest) {
        DeckEntity deckEntity = deckRepository.findById(deckId).orElseThrow(() -> new NotFoundException("Deck not found with id: " + deckId));
        ReviewEntity.ReviewEntityBuilder builder = ReviewEntity.builder();
        builder.deck(deckEntity);
        builder.user(userMapper.toEntity(user));
        builder.rating(reviewCreateRequest.getRating());
        builder.comment(reviewCreateRequest.getComment());
        ReviewEntity reviewEntity = builder.build();

        if (reviewEntity.isValidForCreate()) {
            if(reviewEntity.getRating() > 5.0)
            {
                reviewEntity.setRating(5.0);
            }
            ReviewEntity savedReview = reviewRepository.save(reviewEntity);
            return reviewMapper.toResponse(reviewMapper.toDomain(savedReview));
        } else {
            throw new IllegalArgumentException("Review must have a rating between 0 and 5.");
        }
    }

    @Override
    public ReviewResponse updateReview(ReviewUpdateRequest reviewUpdateRequest) {
        return null;
    }

    @Override
    public void deleteReview(Integer reviewId) {

    }
}
