package com.backend.myspace.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.backend.myspace.model.ContestDTO;

import reactor.core.publisher.Mono;

@Service
public class ContestAggregatorService {

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private CodeforcesService codeforcesService;

    public Mono<List<ContestDTO>> getAllUpcomingContests() {
        Mono<List<ContestDTO>> leetcode = leetCodeService.getUpcomingContestsDTO();
        Mono<List<ContestDTO>> codeforces = codeforcesService.getUpcomingContestsDTO();

        return Mono.zip(leetcode, codeforces)
                .map(tuple -> {
                    List<ContestDTO> all = new ArrayList<>();
                    all.addAll(tuple.getT1());
                    all.addAll(tuple.getT2());

                    return all.stream()
                            .filter(contest -> contest.startTime() > 0)
                            .sorted(Comparator.comparingLong(ContestDTO::startTime))
                            .toList();
                })
                .onErrorReturn(new ArrayList<>());
    }

}
