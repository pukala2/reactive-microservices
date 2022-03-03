package com.reactivespringservice.service;

import com.reactivespringservice.entity.MovieInfo;
import com.reactivespringservice.repository.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MovieInfoService {

    private final MovieInfoRepository movieInfoRepository;

    public MovieInfoService(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Flux<MovieInfo> getStudents() {
        return movieInfoRepository.findAll().log();
    }

    public Mono<MovieInfo> add(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo).log();
    }

    public Mono<MovieInfo> getMovieInfoById(Long id) {
        return movieInfoRepository.findById(id).log();
    }

    public Mono<MovieInfo> updateMovieInfoByName(MovieInfo movieInfo, Long id) {
        return movieInfoRepository.findById(id).flatMap(m -> {
            m.setActors(movieInfo.getActors());
            m.setName(movieInfo.getName());
            m.setYear(movieInfo.getYear());
            m.setReleaseDate(movieInfo.getReleaseDate());
            return movieInfoRepository.save(m);
        }).log();
    }

    public Mono<Void> deleteMovieInfo(Long id) {
        return movieInfoRepository.deleteById(id);
    }
}
