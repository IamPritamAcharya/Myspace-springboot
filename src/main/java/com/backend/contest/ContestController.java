package com.backend.contest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ContestController {

    private final ContestAggregatorService contestAggregatorService;

    public ContestController(ContestAggregatorService contestAggregatorService) {
        this.contestAggregatorService = contestAggregatorService;
    }

    @GetMapping("/contests")
    public Mono<List<ContestDto>> getAllUpcomingContests() {
        return contestAggregatorService.getAllUpcomingContests();
    }
}
