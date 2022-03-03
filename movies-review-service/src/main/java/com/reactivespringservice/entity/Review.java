package com.reactivespringservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    private Long reviewId;
    private Long movieInfoId;
    private String comment;
    private Double rating;
}
