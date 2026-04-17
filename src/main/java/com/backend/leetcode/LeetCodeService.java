package com.backend.leetcode;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LeetCodeService {

    private final WebClient webClient;

    public LeetCodeService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://leetcode.com")
                .defaultHeader("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Referer", "https://leetcode.com")
                .defaultHeader("Origin", "https://leetcode.com")
                .defaultHeader("Content-Type", "application/json")
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

        return executeGraphQL(query, Map.of("username", username));
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
        if (year != null) {
            variables.put("year", year);
        }

        return executeGraphQL(query, variables);
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

        return executeGraphQL(query, Map.of("username", username));
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

        return executeGraphQL(query, Map.of());
    }

    /**
     * Executes a GraphQL query against the LeetCode API with retry and error
     * handling.
     */
    private Mono<Object> executeGraphQL(String query, Map<String, Object> variables) {
        return webClient.post()
                .uri("/graphql")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("query", query, "variables", Map.of()))
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(Duration.ofSeconds(8))
                .retry(1)
                .onErrorResume(e -> {
                    log.warn("LeetCode API call failed: {}", e.getMessage());
                    return Mono.just(Map.of());
                });
    }
}
