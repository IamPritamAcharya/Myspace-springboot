package com.backend.myspace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.backend.myspace.service.LeetCodeService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/leetcode")
public class LeetCodeController {

    @Autowired
    private LeetCodeService leetcodeService;

    @GetMapping("/submissions-recent/{username}")
    public Mono<Object> getRecentSubmissions(@PathVariable String username) {
        return leetcodeService.getRecentSubmissions(username);
    }

    @GetMapping("/problem-solved/{username}")
    public Mono<Object> getUserProblemsSolved(@PathVariable String username) {
        return leetcodeService.getUserProblemsSolved(username);
    }

    @GetMapping("/submissions-calender/{username}")
    public Mono<Object> getSubmissionCalender(@PathVariable String username,
            @RequestParam(required = false) Integer year) {
        return leetcodeService.getUserCalender(username, year);
    }

    @GetMapping("/contests")
    public Mono<Object> getUpcomingContests() {
        return leetcodeService.getUpcomingContests();
    }
}