package com.backend.myspace.model;

import lombok.Data;

@Data
public class RatingChange {
    private String contestName;
    private int ratingUpdateTimeSeconds;
    private int newRating;
}
