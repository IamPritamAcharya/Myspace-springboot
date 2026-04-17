package com.backend.contest;

public record ContestDto(
        String name,
        String platform,
        long startTime,
        String link) {
}
