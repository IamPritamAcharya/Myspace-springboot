package com.backend.contest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.backend.codechef.CodechefContest;
import com.backend.codechef.CodechefService;
import com.backend.codeforces.CodeforcesContest;
import com.backend.codeforces.CodeforcesService;
import com.backend.leetcode.LeetCodeService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ContestAggregatorService {

    private static final DateTimeFormatter CODECHEF_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss", Locale.ENGLISH);
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final LeetCodeService leetCodeService;
    private final CodeforcesService codeforcesService;
    private final CodechefService codechefService;

    public ContestAggregatorService(LeetCodeService leetCodeService,
            CodeforcesService codeforcesService,
            CodechefService codechefService) {
        this.leetCodeService = leetCodeService;
        this.codeforcesService = codeforcesService;
        this.codechefService = codechefService;
    }

    public Mono<List<ContestDto>> getAllUpcomingContests() {
        Mono<PlatformResult> leetcode = leetCodeService.getUpcomingContests()
                .map(this::convertLeetCodeResponse)
                .map(list -> new PlatformResult("LeetCode", list, null))
                .onErrorResume(e -> Mono.just(new PlatformResult("LeetCode", List.of(), e)));

        Mono<PlatformResult> codeforces = codeforcesService.getUpcomingContests()
                .map(this::convertCodeforcesResponse)
                .map(list -> new PlatformResult("Codeforces", list, null))
                .onErrorResume(e -> Mono.just(new PlatformResult("Codeforces", List.of(), e)));

        Mono<PlatformResult> codechef = codechefService.getUpcomingContests()
                .map(this::convertCodechefResponse)
                .map(list -> new PlatformResult("CodeChef", list, null))
                .onErrorResume(e -> Mono.just(new PlatformResult("CodeChef", List.of(), e)));

        return Mono.zip(leetcode, codeforces, codechef)
                .map(tuple -> {
                    PlatformResult l = tuple.getT1();
                    PlatformResult c = tuple.getT2();
                    PlatformResult ch = tuple.getT3();

                    List<ContestDto> all = new ArrayList<>();
                    List<Throwable> errors = new ArrayList<>();

                    all.addAll(l.contests());
                    all.addAll(c.contests());
                    all.addAll(ch.contests());

                    if (l.error() != null) errors.add(l.error());
                    if (c.error() != null) errors.add(c.error());
                    if (ch.error() != null) errors.add(ch.error());

                    List<ContestDto> validContests = all.stream()
                            .filter(contest -> contest.startTime() > 0)
                            .sorted(Comparator.comparingLong(ContestDto::startTime))
                            .toList();

                    if (validContests.isEmpty() && !errors.isEmpty()) {
                        throw new RuntimeException("All contest APIs failed: " + errors);
                    }

                    return validContests;
                });
    }

    // ---- Platform-specific converters ----

    private List<ContestDto> convertCodeforcesResponse(List<CodeforcesContest> contests) {
        return contests.stream()
                .map(contest -> new ContestDto(
                        contest.name(),
                        "Codeforces",
                        contest.startTimeSeconds(),
                        "https://codeforces.com/contest/" + contest.id()))
                .toList();
    }

    private List<ContestDto> convertCodechefResponse(List<CodechefContest> contests) {
        return contests.stream()
                .map(contest -> new ContestDto(
                        contest.getContestName(),
                        "CodeChef",
                        parseCodechefDate(contest.getStartDate()),
                        "https://www.codechef.com/" + contest.getContestCode()))
                .filter(dto -> dto.startTime() > 0)
                .toList();
    }

    private List<ContestDto> convertLeetCodeResponse(Object response) {
        if (!(response instanceof Map<?, ?> responseMap)) {
            return List.of();
        }

        Object dataObj = responseMap.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            return List.of();
        }

        Object upcomingObj = dataMap.get("upcomingContests");
        if (!(upcomingObj instanceof List<?> upcomingList)) {
            return List.of();
        }

        List<ContestDto> result = new ArrayList<>();
        for (Object contestObj : upcomingList) {
            if (!(contestObj instanceof Map<?, ?> contestMap)) {
                continue;
            }

            String title = asString(contestMap.get("title"));
            String titleSlug = asString(contestMap.get("titleSlug"));
            Number startTime = asNumber(contestMap.get("startTime"));

            if (title != null && titleSlug != null && startTime != null) {
                result.add(new ContestDto(
                        title,
                        "LeetCode",
                        startTime.longValue(),
                        "https://leetcode.com/contest/" + titleSlug));
            }
        }

        return result;
    }

    // ---- Utility methods ----

    private long parseCodechefDate(String dateStr) {
        try {
            String normalized = dateStr.replaceAll("\\s+", " ").trim();
            LocalDateTime dateTime = LocalDateTime.parse(normalized, CODECHEF_FORMATTER);
            return dateTime.atZone(IST).toEpochSecond();
        } catch (Exception e) {
            log.warn("Failed to parse CodeChef date: {}", dateStr);
            return 0L;
        }
    }

    private String asString(Object value) {
        return value instanceof String s ? s : null;
    }

    private Number asNumber(Object value) {
        return value instanceof Number n ? n : null;
    }

    private record PlatformResult(String platform, List<ContestDto> contests, Throwable error) {
    }
}
