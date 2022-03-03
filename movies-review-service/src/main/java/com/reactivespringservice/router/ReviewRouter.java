package com.reactivespringservice.router;

import com.reactivespringservice.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler) {
        return route()
                .nest(path("/v1/review"), builder -> {
                    builder
                            .GET("", reviewHandler::getReviews)
                            .POST("", reviewHandler::addReview)
                            .PUT("/{id}", reviewHandler::updateReviewById)
                            .DELETE("/{id}", reviewHandler::deleteById);
                })
                .build();
    }
}
