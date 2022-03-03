package com.reactivespringservice.repository;

import com.reactivespringservice.entity.MovieInfo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieInfoRepository extends ReactiveCrudRepository<MovieInfo, Long> {
}
