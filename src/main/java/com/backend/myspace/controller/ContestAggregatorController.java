package com.backend.myspace.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.myspace.model.ContestDTO;
import com.backend.myspace.service.ContestAggregatorService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")  
public class ContestAggregatorController {
    
    @Autowired
    private ContestAggregatorService contestAggregatorService;
    
    @GetMapping("/contests")  
    public Mono<List<ContestDTO>> getAllUpcomingContests() {
        return contestAggregatorService.getAllUpcomingContests();
    }
}