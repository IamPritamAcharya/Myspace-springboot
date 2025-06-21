package com.backend.myspace.controller;

import org.springframework.web.bind.annotation.*;

import com.backend.myspace.model.CodeforcesContest;
import com.backend.myspace.model.ContestDTO;
import com.backend.myspace.model.RatingChange;
import com.backend.myspace.service.CodeforcesService;

import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/codeforces")
public class CodeforcesController {

    private final CodeforcesService service;

    public CodeforcesController(CodeforcesService service) {
        this.service = service;
    }

    @GetMapping("/rating/{handle}")
    public Mono<List<RatingChange>> getRatingHistory(@PathVariable String handle) {
        return service.getRatingHistory(handle);
    }

    @GetMapping("/contests")
    public Mono<List<CodeforcesContest>> getUpcomingContests() {
        return service.getUpcomingContests(); 
    }
}
