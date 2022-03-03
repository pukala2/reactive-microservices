package com.reactivespringservice.repository;

import com.reactivespringservice.entity.Review;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReviewRepository extends ReactiveCrudRepository<Review, Long> {
    Flux<Review> findReviewsByMovieInfoId(Long movieInfoId);
}
