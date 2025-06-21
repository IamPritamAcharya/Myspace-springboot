package com.backend.myspace.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Service
public class GithubService {
  private final WebClient webClient;

  public GithubService() {
    this.webClient = WebClient.builder()
        .baseUrl("https://api.github.com/graphql")
        .defaultHeader("Content-Type", "application/json")
        .build();
  }

  public Mono<Object> fetchContributions(String username, String token) {
    String query = """
        query {
            user(login: "%s") {
                contributionsCollection {
                    contributionCalendar {
                        weeks {
                            contributionDays {
                                date
                                contributionCount
                            }
                        }
                    }
                }
            }
        }
        """.formatted(username);

    Map<String, String> requestBody = Map.of("query", query);

    return webClient.post()
        .header("Authorization", "Bearer " + token)
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(Object.class);
  }
}