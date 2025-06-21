package com.backend.myspace.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.backend.myspace.model.News;
import com.backend.myspace.service.NewsService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NewsController {
    
    private final NewsService newsService;
    
    @GetMapping("/tech")
    public Flux<News> getTechNews() {
        return newsService.getTechNews();
    }
}