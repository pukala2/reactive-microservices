package com.reactivespringservice.handler;

import com.reactivespringservice.entity.Review;
import com.reactivespringservice.repository.ReviewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.lang.Long.parseLong;

@Component
public class ReviewHandler {

    private final ReviewRepository reviewRepository;

    public ReviewHandler(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .flatMap(reviewRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue).log();
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {

        var movieInfoId = request.queryParam("movieInfoId");
        var reviews = movieInfoId
                .map(e -> reviewRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get())))
                .orElse(reviewRepository.findAll());

        return buildReviewsResponse(reviews);
    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);
    }

    public Mono<ServerResponse> updateReviewById(ServerRequest request) {

        var foundedReview = reviewRepository.findById(parseLong(request.pathVariable("id")));

        return updateReview(request, foundedReview).flatMap(reviewRepository::save)
                .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)).log();
    }

    private Mono<Review> updateReview(ServerRequest request, Mono<Review> foundedMovie) {
        return foundedMovie
                .flatMap(review -> request.bodyToMono(Review.class)
                        .map(requestReview -> {
                            review.setComment(requestReview.getComment());
                            review.setRating(requestReview.getRating());
                            return review;
                        }));
    }

    public Mono<ServerResponse> deleteById(ServerRequest request) {
        var foundedReview = reviewRepository.findById(parseLong(request.pathVariable("id")));
        return foundedReview.flatMap(reviewRepository::delete).then(ServerResponse.noContent().build());
    }
}
