package com.backend.myspace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.myspace.service.GithubService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/github")
public class GithubController {

    @Autowired
    private GithubService githubService;

    @GetMapping("/contributions")
    public Mono<Object> getContributions(
            @RequestParam String username,
            @RequestHeader("X-GitHub-Token") String token) {
        return githubService.fetchContributions(username, token);
    }
}