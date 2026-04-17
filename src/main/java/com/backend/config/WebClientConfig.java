package com.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Centralized WebClient configuration.
 * All services should inject {@link WebClient.Builder} to get consistent defaults.
 */
@Configuration
public class WebClientConfig {

    private static final int MAX_IN_MEMORY_SIZE = 2 * 1024 * 1024; // 2 MB

    @Bean
    public WebClient.Builder webClientBuilder() {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .build();

        return WebClient.builder().exchangeStrategies(strategies);
    }
}
