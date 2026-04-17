package com.backend.codeforces;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class CodeforcesService {

    private final WebClient webClient;

    public CodeforcesService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://codeforces.com/api")
                .build();
    }

    @SuppressWarnings("unchecked")
    public Mono<List<RatingChange>> getRatingHistory(String handle) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user.rating")
                        .queryParam("handle", handle)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("result"))
                .map(list -> list.stream()
                        .map(obj -> {
                            RatingChange rc = new RatingChange();
                            rc.setContestName((String) obj.get("contestName"));
                            rc.setRatingUpdateTimeSeconds(
                                    ((Number) obj.get("ratingUpdateTimeSeconds")).intValue());
                            rc.setNewRating(((Number) obj.get("newRating")).intValue());
                            return rc;
                        })
                        .toList());
    }

    @SuppressWarnings("unchecked")
    public Mono<List<CodeforcesContest>> getUpcomingContests() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/contest.list")
                        .queryParam("gym", false)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("result"))
                .map(list -> list.stream()
                        .filter(obj -> "BEFORE".equals(obj.get("phase")))
                        .map(obj -> new CodeforcesContest(
                                ((Number) obj.get("id")).intValue(),
                                (String) obj.get("name"),
                                (String) obj.get("phase"),
                                ((Number) obj.get("startTimeSeconds")).longValue()))
                        .toList());
    }
}
