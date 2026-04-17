package com.backend.leetcode;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/leetcode")
public class LeetCodeController {

    private final LeetCodeService leetCodeService;

    public LeetCodeController(LeetCodeService leetCodeService) {
        this.leetCodeService = leetCodeService;
    }

    @GetMapping("/submissions-recent/{username}")
    public Mono<Object> getRecentSubmissions(@PathVariable String username) {
        return leetCodeService.getRecentSubmissions(username);
    }

    @GetMapping("/problem-solved/{username}")
    public Mono<Object> getUserProblemsSolved(@PathVariable String username) {
        return leetCodeService.getUserProblemsSolved(username);
    }

    @GetMapping("/submissions-calender/{username}")
    public Mono<Object> getSubmissionCalender(@PathVariable String username,
            @RequestParam(required = false) Integer year) {
        return leetCodeService.getUserCalender(username, year);
    }

    @GetMapping("/contests")
    public Mono<Object> getUpcomingContests() {
        return leetCodeService.getUpcomingContests();
    }
}
