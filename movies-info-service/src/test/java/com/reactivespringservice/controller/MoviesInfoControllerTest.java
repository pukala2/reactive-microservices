package com.reactivespringservice.controller;

//import com.reactivespring.entity.MovieInfo;
//import com.reactivespring.repository.MovieInfoRepository;
import com.reactivespringservice.entity.MovieInfo;
import com.reactivespringservice.repository.MovieInfoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Testcontainers
@SpringBootTest(webEnvironment = RANDOM_PORT)
class MoviesInfoControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    private final static String MOVIE_INFO_URL = "/v1/movieInfo";

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
        var moviesInfo = List.of(
                new MovieInfo(null, "Batman Begins", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight", 2008,
                        List.of( "Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo(null, "Dark Knight Rises", 2012,
                        List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(moviesInfo).log().blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void getMoviesInfo() {
        webTestClient
                .get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void addMovieInfo() {
        webTestClient
                .post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(new MovieInfo(null, "Batman Begins1", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert  savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });
    }

    @Test
    void getMovieInfoById() {

        var foundedMovie = movieInfoRepository.findAll().blockFirst();

        webTestClient
                .get()
                .uri(MOVIE_INFO_URL+"/{id}", Objects.requireNonNull(foundedMovie).getMovieInfoId() + 1L)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("The Dark Knight");
    }

    @Test
    void updateMovieInfo() {
        var updateMovie = new MovieInfo(null, "Bad man", 2005,
                List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        var foundedMovie = movieInfoRepository.findAll().blockFirst();
        webTestClient
                .put()
                .uri(MOVIE_INFO_URL+"/{id}",
                        Objects.requireNonNull(foundedMovie).getMovieInfoId(), updateMovie)
                .bodyValue(updateMovie)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert  updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    Assertions.assertEquals(updateMovie.getName(), updatedMovieInfo.getName());
                });
    }

    @Test
    void deleteMovieInfoById() {

        var foundedMovie = movieInfoRepository.findAll().blockFirst();
        webTestClient
                .delete()
                .uri(MOVIE_INFO_URL+"/{id}", Objects.requireNonNull(foundedMovie).getMovieInfoId())
                .exchange()
                .expectStatus()
                .is2xxSuccessful();

        webTestClient
                .get()
                .uri(MOVIE_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMoviesInfoStream() {

        webTestClient
                .post()
                .uri(MOVIE_INFO_URL)
                .bodyValue(new MovieInfo(null, "Batman Begins1", 2005,
                        List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert  savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoId() != null;
                });

        var moviesStreamFlux = webTestClient
                .get()
                .uri(MOVIE_INFO_URL+"/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                .getResponseBody();

        StepVerifier.create(moviesStreamFlux)
                .assertNext(movieInfo -> {
                    assert movieInfo.getMovieInfoId() != null;
                })
                .thenCancel()
                .verify();
    }

}