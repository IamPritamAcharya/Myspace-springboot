package com.backend.gfg;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gfg")
public class GfgController {

    private final GfgService gfgService;

    public GfgController(GfgService gfgService) {
        this.gfgService = gfgService;
    }

    @GetMapping("/heatmap/{userHandle}")
    public Mono<Map<String, Integer>> getHeatMap(@PathVariable String userHandle) {
        return gfgService.getHeatMap(userHandle);
    }
}
