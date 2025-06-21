package com.backend.myspace.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.backend.myspace.model.ContestDTO;

import reactor.core.publisher.Mono;

@Service
public class LeetCodeService {

    private final WebClient webClient;

    public LeetCodeService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://leetcode.com")
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Referer", "https://leetcode.com/")
                .build();
    }

    public Mono<Object> getRecentSubmissions(String username) {
        String query = """
                query recentSubmissions($username: String!) {
                    recentSubmissionList(username: $username) {
                        title
                        titleSlug
                        timestamp
                        statusDisplay
                        lang
                    }
                }
                """;

        return webClient.post()
                .uri("/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("query", query, "variables", Map.of("username", username)))
                .retrieve()
                .bodyToMono(Object.class);
    }

    public Mono<Object> getUserCalender(String username, Integer year) {
        String query = """
                query userProfileCalendar($username: String!, $year: Int) {
                    matchedUser(username: $username) {
                        userCalendar(year: $year) {
                            activeYears
                            streak
                            totalActiveDays
                            dccBadges {
                                timestamp
                                badge {
                                    name
                                    icon
                                }
                            }
                            submissionCalendar
                        }
                    }
                }
                """;

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", username);
        if (year != null)
            variables.put("year", year);

        return webClient.post()
                .uri("/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("query", query, "variables", variables))
                .retrieve()
                .bodyToMono(Object.class);
    }

    public Mono<Object> getUserProblemsSolved(String username) {
        String query = """
                query userProblemsSolved($username: String!) {
                    allQuestionsCount {
                        difficulty
                        count
                    }
                    matchedUser(username: $username) {
                        problemsSolvedBeatsStats {
                            difficulty
                            percentage
                        }
                        submitStatsGlobal {
                            acSubmissionNum {
                                difficulty
                                count
                            }
                        }
                    }
                }
                """;

        return webClient.post()
                .uri("/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("query", query, "variables", Map.of("username", username)))
                .retrieve()
                .bodyToMono(Object.class);
    }

    public Mono<Object> getUpcomingContests() {
        String query = """
                    query {
                        upcomingContests {
                            title
                            titleSlug
                            startTime
                            duration
                        }
                    }
                """;
        return webClient.post()
                .uri("/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("query", query, "variables", Map.of()))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ((Map<?, ?>) response.get("data")).get("upcomingContests"));
    }

    public Mono<List<ContestDTO>> getUpcomingContestsDTO() {
        return getUpcomingContests()
                .map(contestsData -> {
                    List<ContestDTO> contests = new ArrayList<>();
                    if (contestsData instanceof List<?> list) {
                        for (Object contest : list) {
                            if (contest instanceof Map<?, ?> contestMap) {
                                String title = (String) contestMap.get("title");
                                String titleSlug = (String) contestMap.get("titleSlug");
                                Object startTimeObj = contestMap.get("startTime");

                                if (title != null && titleSlug != null && startTimeObj instanceof Number) {
                                    contests.add(new ContestDTO(
                                            title,
                                            "LeetCode",
                                            ((Number) startTimeObj).longValue(),
                                            "https://leetcode.com/contest/" + titleSlug));
                                }
                            }
                        }
                    }
                    return contests;
                })
                .onErrorReturn(new ArrayList<>());
    }
}