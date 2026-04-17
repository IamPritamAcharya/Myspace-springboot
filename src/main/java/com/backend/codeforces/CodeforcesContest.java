package com.backend.codeforces;

public record CodeforcesContest(
        Integer id,
        String name,
        String phase,
        Long startTimeSeconds) {
}
