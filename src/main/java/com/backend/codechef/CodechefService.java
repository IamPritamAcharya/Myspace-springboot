package com.backend.codechef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CodechefService {

    private final WebClient webClient;

    public CodechefService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://www.codechef.com")
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public Mono<List<CodechefContest>> getUpcomingContests() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/list/contests/all")
                        .queryParam("sort_by", "START")
                        .queryParam("sorting_order", "asc")
                        .queryParam("offset", "0")
                        .queryParam("mode", "all")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(CodechefApiResponse.class)
                .map(response -> {
                    if (response == null) {
                        return Collections.<CodechefContest>emptyList();
                    }

                    List<CodechefContest> result = new ArrayList<>();
                    if (response.getFutureContests() != null) {
                        result.addAll(response.getFutureContests());
                    }
                    if (response.getPresentContests() != null) {
                        result.addAll(response.getPresentContests());
                    }
                    return result;
                })
                .onErrorResume(e -> {
                    log.warn("CodeChef API call failed: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                });
    }
}