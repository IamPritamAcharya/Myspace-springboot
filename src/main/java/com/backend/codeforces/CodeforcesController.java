package com.backend.codeforces;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/codeforces")
public class CodeforcesController {

    private final CodeforcesService codeforcesService;

    public CodeforcesController(CodeforcesService codeforcesService) {
        this.codeforcesService = codeforcesService;
    }

    @GetMapping("/rating/{handle}")
    public Mono<List<RatingChange>> getRatingHistory(@PathVariable String handle) {
        return codeforcesService.getRatingHistory(handle);
    }

    @GetMapping("/contests")
    public Mono<List<CodeforcesContest>> getUpcomingContests() {
        return codeforcesService.getUpcomingContests();
    }
}
