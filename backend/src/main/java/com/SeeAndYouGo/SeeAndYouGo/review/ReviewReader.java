package com.SeeAndYouGo.SeeAndYouGo.review;

import com.SeeAndYouGo.SeeAndYouGo.global.exception.EntityNotFoundException;
import com.SeeAndYouGo.SeeAndYouGo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ReviewReader {
    private final ReviewRepository reviewRepository;

    public Review getById(Long id) {
        return getById(id, null);
    }

    public Review getById(Long id, String userMessage) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.REVIEW_NOT_FOUND, "id=" + id, userMessage));
    }

    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }
}
