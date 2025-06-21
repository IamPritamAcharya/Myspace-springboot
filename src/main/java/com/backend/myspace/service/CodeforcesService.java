package com.backend.myspace.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.backend.myspace.model.CodeforcesContest;
import com.backend.myspace.model.ContestDTO;
import com.backend.myspace.model.RatingChange;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CodeforcesService {

        private final WebClient webClient;

        public CodeforcesService(WebClient.Builder builder) {
                ExchangeStrategies strategies = ExchangeStrategies.builder()
                                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                                .build();
                this.webClient = builder
                                .baseUrl("https://codeforces.com/api")
                                .exchangeStrategies(strategies)
                                .build();
        }

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
                                                                        (Integer) obj.get("ratingUpdateTimeSeconds"));
                                                        rc.setNewRating((Integer) obj.get("newRating"));
                                                        return rc;
                                                })
                                                .toList());
        }

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
                                                .filter(obj -> "BEFORE".equals(obj.get("phase"))) // Filter only
                                                                                                  // upcoming contests
                                                .map(obj -> new CodeforcesContest(
                                                                (Integer) obj.get("id"),
                                                                (String) obj.get("name"),
                                                                (String) obj.get("phase"),
                                                                ((Number) obj.get("startTimeSeconds")).longValue()))
                                                .toList());
        }

        public Mono<List<ContestDTO>> getUpcomingContestsDTO() {
                return getUpcomingContests()
                                .map(contests -> contests.stream()
                                                .map(contest -> new ContestDTO(
                                                                contest.name(),
                                                                "Codeforces",
                                                                contest.startTimeSeconds(),
                                                                "https://codeforces.com/contest/" + contest.id()))
                                                .toList())
                                .onErrorReturn(new ArrayList<>());
        }

}
