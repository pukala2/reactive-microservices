package com.reactivespringservice.controller;

import com.reactivespringservice.entity.MovieInfo;
import com.reactivespringservice.service.MovieInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/v1/movieInfo")
public class MoviesInfoController {

    private final MovieInfoService movieInfoService;

    Sinks.Many<MovieInfo> movieInfoSink = Sinks.many().replay().all();

    public MoviesInfoController(MovieInfoService movieInfoService) {
        this.movieInfoService = movieInfoService;
    }

    @GetMapping()
    public ResponseEntity<Flux<MovieInfo>> getMoviesInfo() {
        return new ResponseEntity<>(movieInfoService.getStudents(), HttpStatus.OK);
    }

    @PostMapping()
    public Mono<ResponseEntity<MovieInfo>> addMovieInfo(@RequestBody MovieInfo movieInfo) {
        return movieInfoService.add(movieInfo).doOnNext(savedInfo -> movieInfoSink.tryEmitNext(savedInfo))
                .map(ResponseEntity.status(HttpStatus.CREATED)::body);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById(@PathVariable Long id) {
        return movieInfoService.getMovieInfoById(id).map(ResponseEntity.ok()::body).switchIfEmpty(
                Mono.just(ResponseEntity.notFound().build())).log();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> getMovieInfoById() {
        return movieInfoSink.asFlux().log();
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfoById(@RequestBody MovieInfo movieInfo, @PathVariable Long id) {
        return movieInfoService.updateMovieInfoByName(movieInfo, id)
                .map(ResponseEntity.ok()::body).switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovieById(@PathVariable Long id) {
        movieInfoService.deleteMovieInfo(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
