package com.reactivespringservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
public class MovieInfo {

    @Id
    private Long movieInfoId;
    private String name;
    private Integer year;
    private List<String> actors;
    private LocalDate releaseDate;

}
