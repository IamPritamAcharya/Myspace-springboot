package com.backend.myspace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.backend.myspace.service.GfgHeatmapService;

import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("api/gfg")
public class GfgController {

    @Autowired
    private GfgHeatmapService gfgHeatmapService;

    @GetMapping("/heatmap/{userHandle}")
    public Mono<Map<String, Integer>> getHeatMap(@PathVariable String userHandle) {
        return gfgHeatmapService.getHeatMap(userHandle);
    }
}
