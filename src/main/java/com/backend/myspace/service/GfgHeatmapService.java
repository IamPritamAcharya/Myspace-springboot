package com.backend.myspace.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.backend.myspace.model.GfgHeatmapResponse;

import reactor.core.publisher.Mono;

@Service
public class GfgHeatmapService {

    private final WebClient webClient;

    public GfgHeatmapService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.geeksforgeeks.org").build();
    }

    public Mono<Map<String, Integer>> getHeatMap(String userHandle) {
        return webClient
                .get()
                .uri("/gfg-assets/_next/data/SEZyqWa8pXrnCAKT5xxuV/user/{userHandle}.json", userHandle)
                .retrieve()
                .bodyToMono(GfgHeatmapResponse.class)
                .map(response -> response.getPageProps().getHeatMapData().getResult());

    }

}
