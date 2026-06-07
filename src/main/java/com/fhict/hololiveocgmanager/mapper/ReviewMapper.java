package com.fhict.hololiveocgmanager.mapper;

import com.fhict.hololiveocgmanager.domain.Review;
import com.fhict.hololiveocgmanager.dto.response.ReviewResponse;
import com.fhict.hololiveocgmanager.entity.ReviewEntity;
import org.springframework.stereotype.Service;

@Service
public class ReviewMapper {
    public ReviewEntity toEntity(Review review) {
        ReviewEntity.ReviewEntityBuilder builder = ReviewEntity.builder();
        builder.id(review.getId());
        if (review.getDeck() != null) {
            builder.deck(new DeckMapper().toEntity(review.getDeck()));
        }
        if (review.getUser() != null) {
            builder.user(new UserMapper().toEntity(review.getUser()));
        }
        builder.rating(review.getRating());
        builder.comment(review.getComment());
        return builder.build();
    }

    public Review toDomain(ReviewEntity reviewEntity) {
        Review.ReviewBuilder builder = Review.builder();
        builder.id(reviewEntity.getId());
        builder.deck(new DeckMapper().toDomain(reviewEntity.getDeck()));
        builder.user(new UserMapper().toDomain(reviewEntity.getUser()));
        builder.rating(reviewEntity.getRating());
        builder.comment(reviewEntity.getComment());
        return builder.build();
    }

    public ReviewResponse toResponse(Review review) {
        ReviewResponse.ReviewResponseBuilder builder = ReviewResponse.builder();
        builder.id(review.getId());
        builder.deck(new DeckMapper().toResponse(review.getDeck()));
        builder.user(new UserMapper().toResponse(review.getUser()));
        builder.rating(review.getRating());
        builder.comment(review.getComment());
        return builder.build();
    }
}
