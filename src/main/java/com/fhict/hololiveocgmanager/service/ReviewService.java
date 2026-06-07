package com.fhict.hololiveocgmanager.service;

import com.fhict.hololiveocgmanager.domain.User;
import com.fhict.hololiveocgmanager.dto.request.ReviewCreateRequest;
import com.fhict.hololiveocgmanager.dto.request.ReviewUpdateRequest;
import com.fhict.hololiveocgmanager.dto.response.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewService {
    Page<ReviewResponse> getReviewsByDeckId(Integer deckId, Pageable pageable);
    ReviewResponse createReview(User user, Integer deckId, ReviewCreateRequest reviewCreateRequest);
    ReviewResponse updateReview(ReviewUpdateRequest reviewUpdateRequest);
    void deleteReview(Integer reviewId);
}
