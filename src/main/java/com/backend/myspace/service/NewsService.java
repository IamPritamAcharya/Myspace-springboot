package com.backend.myspace.service;

import com.backend.myspace.model.News;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {

    private final WebClient webClient;
    private final GeminiService geminiService;

    @Value("${news.api.key:pub_9c114068d5204b62b63f7f0e2087bd59}")
    private String newsApiKey;

    @Value("${news.cache.duration:300}")
    private int cacheDurationSeconds;

    @Value("${news.max.articles:15}")
    private int maxArticles;

    // Enhanced programming keywords with weighted scoring
    private static final Map<String, Integer> PROGRAMMING_KEYWORDS = new HashMap<>() {{
        // Core Programming Languages (High Weight)
        put("javascript", 10); put("python", 10); put("java", 10); put("typescript", 10);
        put("c++", 9); put("c#", 9); put("golang", 9); put("rust", 9); put("kotlin", 9);
        put("swift", 9); put("php", 8); put("ruby", 8); put("scala", 8); put("perl", 7);
        
        // Web Technologies (High Weight)
        put("react", 10); put("angular", 9); put("vue", 9); put("nodejs", 9); put("nextjs", 9);
        put("express", 8); put("django", 8); put("flask", 8); put("spring boot", 9);
        put("laravel", 8); put("rails", 8); put("fastapi", 8);
        
        // Cloud & DevOps (High Weight)
        put("aws", 9); put("azure", 9); put("gcp", 8); put("docker", 9); put("kubernetes", 9);
        put("ci/cd", 8); put("jenkins", 7); put("terraform", 8); put("ansible", 7);
        put("microservices", 8); put("serverless", 8);
        
        // AI/ML (Very High Weight - trending)
        put("machine learning", 10); put("artificial intelligence", 10); put("deep learning", 10);
        put("neural networks", 9); put("tensorflow", 9); put("pytorch", 9); put("openai", 10);
        put("chatgpt", 9); put("llm", 10); put("generative ai", 10); put("computer vision", 8);
        put("nlp", 8); put("data science", 9); put("big data", 8);
        
        // Database Technologies
        put("sql", 7); put("nosql", 7); put("mongodb", 8); put("postgresql", 8);
        put("mysql", 7); put("redis", 8); put("elasticsearch", 8); put("cassandra", 7);
        
        // Mobile Development
        put("android", 8); put("ios", 8); put("flutter", 9); put("react native", 9);
        put("xamarin", 7); put("cordova", 6);
        
        // Emerging Technologies
        put("blockchain", 8); put("web3", 8); put("cryptocurrency", 7); put("nft", 6);
        put("metaverse", 7); put("augmented reality", 7); put("virtual reality", 7);
        put("iot", 7); put("edge computing", 8);
        
        // Development Practices & Tools
        put("agile", 6); put("scrum", 6); put("devops", 8); put("git", 7); put("github", 8);
        put("gitlab", 7); put("api", 8); put("rest api", 8); put("graphql", 8);
        put("microservices", 8); put("clean code", 7); put("tdd", 7); put("unit testing", 7);
        
        // Security
        put("cybersecurity", 8); put("encryption", 7); put("oauth", 7); put("jwt", 7);
        put("penetration testing", 7); put("vulnerability", 7);
        
        // General Tech Terms
        put("open source", 7); put("programming", 8); put("coding", 8); put("software development", 9);
        put("web development", 9); put("mobile development", 8); put("full stack", 8);
        put("backend", 8); put("frontend", 8); put("tech startup", 7); put("saas", 7);
    }};

    // Exclusion patterns for better filtering
    private static final Set<String> EXCLUSION_KEYWORDS = Set.of(
        "sports", "entertainment", "celebrity", "politics", "election", "weather",
        "health", "medical", "food", "travel", "fashion", "beauty", "lifestyle",
        "gossip", "music", "movie", "film", "tv show", "reality tv", "dating",
        "relationship", "wedding", "divorce", "pregnancy", "baby", "parenting"
    );

    // Compiled regex patterns for better performance
    private static final Pattern TECH_COMPANY_PATTERN = Pattern.compile(
        "\\b(google|microsoft|apple|amazon|meta|facebook|twitter|netflix|uber|airbnb|tesla|nvidia|intel|amd|oracle|salesforce|adobe|spotify|slack|zoom|dropbox|github|gitlab|atlassian|jetbrains|docker|kubernetes|mongodb|redis|elastic)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PROGRAMMING_CONCEPT_PATTERN = Pattern.compile(
        "\\b(algorithm|data structure|design pattern|architecture|framework|library|sdk|api|database|server|client|frontend|backend|fullstack|devops|cicd|testing|debugging|refactoring|deployment|scalability|performance|optimization)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // Cache for relevance scores to avoid recalculation
    private final Map<String, Integer> relevanceCache = new ConcurrentHashMap<>();

    @Cacheable(value = "techNews", unless = "#result.collectList().block().isEmpty()")
    public Flux<News> getTechNews() {
        return fetchNewsFromMultipleSources()
                .filter(this::isHighQualityTechArticle)
                .sort(this::compareByRelevance)
                .take(maxArticles)
                .flatMap(this::convertToNews)
                .doOnError(throwable -> log.error("Error in getTechNews: {}", throwable.getMessage()))
                .onErrorResume(throwable -> Flux.empty());
    }

    private Flux<JsonNode> fetchNewsFromMultipleSources() {
        // Fetch from multiple categories and queries for better coverage
        return Flux.merge(
                fetchNewsWithQuery("programming OR coding OR software"),
                fetchNewsWithQuery("artificial intelligence OR machine learning"),
                fetchNewsWithQuery("web development OR mobile development"),
                fetchNewsWithQuery("cloud computing OR devops"),
                fetchNewsWithQuery("blockchain OR cryptocurrency")
        ).distinct(article -> getTextValue(article, "title")); // Remove duplicates
    }

    private Flux<JsonNode> fetchNewsWithQuery(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("newsdata.io")
                        .path("/api/1/news")
                        .queryParam("apikey", newsApiKey)
                        .queryParam("language", "en")
                        .queryParam("category", "technology")
                        .queryParam("q", query)
                        .queryParam("size", "10")
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(10))
                .flatMapMany(response -> {
                    JsonNode results = response.get("results");
                    if (results == null || !results.isArray()) {
                        return Flux.empty();
                    }
                    return Flux.fromIterable(results).cast(JsonNode.class);
                })
                .onErrorResume(throwable -> {
                    log.warn("Error fetching news with query '{}': {}", query, throwable.getMessage());
                    return Flux.empty();
                });
    }

    private boolean isHighQualityTechArticle(JsonNode article) {
        String title = getTextValue(article, "title");
        String description = getTextValue(article, "description");
        
        if (title.isEmpty() && description.isEmpty()) {
            return false;
        }

        int relevanceScore = calculateRelevanceScore(title, description);
        
        // Cache the score for potential reuse
        String cacheKey = title + "|" + description;
        relevanceCache.put(cacheKey, relevanceScore);
        
        return relevanceScore >= 15; // Threshold for high-quality tech articles
    }

    private int calculateRelevanceScore(String title, String description) {
        String content = (title + " " + description).toLowerCase();
        int score = 0;

        // Check programming keywords with weights
        for (Map.Entry<String, Integer> entry : PROGRAMMING_KEYWORDS.entrySet()) {
            String keyword = entry.getKey();
            int weight = entry.getValue();
            
            if (content.contains(keyword)) {
                score += weight;
                // Bonus for title mentions
                if (title.toLowerCase().contains(keyword)) {
                    score += weight / 2;
                }
            }
        }

        // Check tech company mentions
        if (TECH_COMPANY_PATTERN.matcher(content).find()) {
            score += 5;
        }

        // Check programming concepts
        if (PROGRAMMING_CONCEPT_PATTERN.matcher(content).find()) {
            score += 8;
        }

        // Penalty for exclusion keywords
        for (String exclusionKeyword : EXCLUSION_KEYWORDS) {
            if (content.contains(exclusionKeyword)) {
                score -= 20; // Heavy penalty
            }
        }

        // Bonus for recent trending topics
        if (content.contains("ai") || content.contains("chatgpt") || content.contains("llm")) {
            score += 10;
        }

        return Math.max(0, score); // Ensure non-negative score
    }

    private int compareByRelevance(JsonNode a, JsonNode b) {
        String titleA = getTextValue(a, "title");
        String descA = getTextValue(a, "description");
        String titleB = getTextValue(b, "title");
        String descB = getTextValue(b, "description");

        String keyA = titleA + "|" + descA;
        String keyB = titleB + "|" + descB;

        int scoreA = relevanceCache.getOrDefault(keyA, calculateRelevanceScore(titleA, descA));
        int scoreB = relevanceCache.getOrDefault(keyB, calculateRelevanceScore(titleB, descB));

        return Integer.compare(scoreB, scoreA); // Higher score first
    }

    private Mono<News> convertToNews(JsonNode article) {
        try {
            String title = getTextValue(article, "title");
            String description = getTextValue(article, "description");
            String url = getTextValue(article, "link");
            String imageUrl = getTextValue(article, "image_url");
            String publishedAt = getTextValue(article, "pubDate");
            String source = getTextValue(article, "source_id");

            if (title.isEmpty()) {
                return Mono.empty();
            }

            String contentToSummarize = buildContentForSummary(title, description);
            String enhancedPrompt = createEnhancedPrompt(contentToSummarize);

            return geminiService.summarize(enhancedPrompt)
                    .map(summary -> new News(title, summary, url, imageUrl, publishedAt, source))
                    .timeout(Duration.ofSeconds(5))
                    .onErrorReturn(new News(title, description, url, imageUrl, publishedAt, source));

        } catch (Exception e) {
            log.error("Error converting article to News: {}", e.getMessage());
            return Mono.empty();
        }
    }

    private String buildContentForSummary(String title, String description) {
        StringBuilder content = new StringBuilder(title);
        if (!description.isEmpty() && !description.equals(title)) {
            content.append(". ").append(description);
        }
        return content.toString();
    }

    private String createEnhancedPrompt(String content) {
        return String.format(
            "As a tech expert, provide a concise 2-3 sentence summary of this programming/tech news. " +
            "Focus on: 1) Key technical details, 2) Impact on developers/industry, 3) Technologies mentioned. " +
            "Make it engaging and informative for software developers: %s", 
            content
        );
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return "";
        }
        String value = field.asText("").trim();
        return value.equals("null") ? "" : value;
    }

    // Method to clear cache periodically (can be called by a scheduled task)
    public void clearRelevanceCache() {
        relevanceCache.clear();
        log.info("Relevance cache cleared");
    }

    // Method to get cache statistics
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "cacheSize", relevanceCache.size(),
            "keywordCount", PROGRAMMING_KEYWORDS.size(),
            "exclusionCount", EXCLUSION_KEYWORDS.size()
        );
    }
}