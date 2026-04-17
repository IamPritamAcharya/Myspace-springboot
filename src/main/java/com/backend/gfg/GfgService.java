package com.backend.gfg;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class GfgService {

    private static final Pattern BUILD_ID_PATTERN =
            Pattern.compile("/_next/static/([a-zA-Z0-9_-]+)/_ssgManifest\\.js");

    private final WebClient webClient;

    public GfgService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://www.geeksforgeeks.org")
                .build();
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
        Matcher matcher = BUILD_ID_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new RuntimeException("Build ID not found in HTML");
    }
}
