package com.backend.myspace.model;

public record CodeforcesContest(
    Integer id,
    String name,
    String phase,
    Long startTimeSeconds
) {

}