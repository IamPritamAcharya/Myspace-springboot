package com.backend.codechef;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/codechef")
public class CodechefController {

    private final CodechefService codechefService;

    public CodechefController(CodechefService codechefService) {
        this.codechefService = codechefService;
    }

    @GetMapping("/contests")
    public Mono<List<CodechefContest>> getUpcomingContests() {
        return codechefService.getUpcomingContests();
    }
}
