package com.reactivespringservice.router;

import com.reactivespringservice.entity.Review;
import com.reactivespringservice.repository.ReviewRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewRouterTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewRepository reviewRepository;

    private final static String REVIEW_URL = "/v1/review";

    static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(DockerImageName
            .parse("pukala93/movieinfopostgres:latest").asCompatibleSubstituteFor("postgres"))
            .withDatabaseName("test")
            .withUsername("duke")
            .withPassword("s3cret")
            .withReuse(true);

    @DynamicPropertySource
    private static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                format("r2dbc:pool:postgresql://%s:%d/%s",
                        postgreSQLContainer.getHost(),
                        postgreSQLContainer.getFirstMappedPort(),
                        postgreSQLContainer.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgreSQLContainer::getUsername);
        registry.add("spring.r2dbc.password", postgreSQLContainer::getPassword);
    }


    @BeforeAll
    static void setUpAll() {
        postgreSQLContainer.start();
    }

    @BeforeEach
    void setUp() {
        var reviewList = List.of(
                new Review(null, 1L, "Awesome movie", 9.0),
                new Review(null, 1L, "Awesome movie1", 9.0),
                new Review(null, 2L, "Excellent Movie", 8.0));
        reviewRepository.saveAll(reviewList).log().blockLast();
    }

    String url = String.format("r2dbc:pool:postgresql://%s:%d/%s",
            postgreSQLContainer.getHost(),
            postgreSQLContainer.getFirstMappedPort(),
            postgreSQLContainer.getDatabaseName());


    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll().block();
    }

    @Test
    void findReviewById() {
        webTestClient
                .get()
                .uri(REVIEW_URL+"/?movieInfoId=1")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }

    @Test
    void getReviews() {
        webTestClient
                .get()
                .uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class);
    }

    @Test
    void addReview() {

        var review = new Review(null, 1L, "Awesome movie", 9.0);
        webTestClient
                .post()
                .uri(REVIEW_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(reviewEntityExchangeResult -> {
                    var savedReview = reviewEntityExchangeResult.getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getMovieInfoId() != null;
                });
    }

    @Test
    void updateReview() {

        var updateReview = new Review(null, 1L, "Awesome movie 3", 9.0);
        var reviewToUpdate = reviewRepository.findAll().blockFirst();

        webTestClient
                .put()
                .uri(REVIEW_URL + "/{id}", Objects.requireNonNull(reviewToUpdate).getReviewId(), updateReview)
                .bodyValue(updateReview)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedReview = movieInfoEntityExchangeResult.getResponseBody();
                    assert updatedReview != null;
                    assert updatedReview.getMovieInfoId() != null;
                    Assertions.assertEquals(updatedReview.getComment(), updatedReview.getComment());
                    Assertions.assertEquals(updatedReview.getRating(), updatedReview.getRating());
                });
    }

    @Test
    void deleteReview() {

        var reviewToDelete = reviewRepository.findAll().blockFirst();

        webTestClient
                .delete()
                .uri(REVIEW_URL + "/{id}", Objects.requireNonNull(reviewToDelete).getReviewId())
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        webTestClient
                .get()
                .uri(REVIEW_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(2);
    }
}