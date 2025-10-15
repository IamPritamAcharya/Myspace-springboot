package com.backend.myspace.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        return fetchBuildIdFromHtml(userHandle)
                .flatMap(buildId -> webClient
                        .get()
                        .uri("/gfg-assets/_next/data/{buildId}/user/{userHandle}.json", buildId, userHandle)
                        .retrieve()
                        .bodyToMono(GfgHeatmapResponse.class)
                        .map(response -> response.getPageProps().getHeatMapData().getResult()));
    }

    private Mono<String> fetchBuildIdFromHtml(String userHandle) {
        return webClient
                .get()
                .uri("/user/{userHandle}", userHandle)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractBuildIdFromHtml);
    }

    private String extractBuildIdFromHtml(String html) {
        Pattern pattern = Pattern.compile("/_next/static/([a-zA-Z0-9_-]+)/_ssgManifest\\.js");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new RuntimeException("Build ID not found in HTML");
        }
    }
}
