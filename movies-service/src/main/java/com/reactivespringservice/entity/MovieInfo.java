package com.reactivespringservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieInfo {
    private Long movieInfoId;
    private String name;
    private Integer year;
    private List<String> actors;
    private LocalDate releaseDate;
}
