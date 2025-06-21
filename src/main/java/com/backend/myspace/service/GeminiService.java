package com.backend.myspace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Service
@RequiredArgsConstructor
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final String GEMINI_API_KEY = "AIzaSyBavhit4LJmccDPfFsh3Lk1HQjYywnO0Kg";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public Mono<String> summarize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Mono.just("No content to summarize");
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            ArrayNode contents = objectMapper.createArrayNode();
            ObjectNode content = objectMapper.createObjectNode();
            ArrayNode parts = objectMapper.createArrayNode();
            ObjectNode part = objectMapper.createObjectNode();

            part.put("text", "Summarize this tech news in 2-3 sentences for mobile app: " + text);
            parts.add(part);
            content.set("parts", parts);
            contents.add(content);
            requestBody.set("contents", contents);

            return webClient.post()
                    .uri(GEMINI_URL + "?key=" + GEMINI_API_KEY)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .map(this::extractSummaryFromResponse)
                    .onErrorReturn("Summary not available")
                    .timeout(java.time.Duration.ofSeconds(10));

        } catch (Exception e) {
            System.err.println("Error creating Gemini request: " + e.getMessage());
            return Mono.just("Summary not available");
        }
    }

    private String extractSummaryFromResponse(JsonNode response) {
        try {
            JsonNode candidates = response.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray() && parts.size() > 0) {
                        JsonNode textNode = parts.get(0).get("text");
                        if (textNode != null) {
                            return textNode.asText("Summary not available");
                        }
                    }
                }
            }
            return "Summary not available";
        } catch (Exception e) {
            System.err.println("Error extracting summary from response: " + e.getMessage());
            return "Summary not available";
        }
    }
}
