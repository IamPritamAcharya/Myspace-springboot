package com.backend.myspace.model;

public record ContestDTO(
        String name,
        String platform,
        long startTime,
        String link) {
}