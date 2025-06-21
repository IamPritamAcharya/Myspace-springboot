package com.backend.myspace.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties
public class GfgHeatmapResponse {

    @JsonProperty("pageProps")
    private PageProps pageProps;

    @Data
    @NoArgsConstructor
    public static class PageProps {

        @JsonProperty("heatMapData")
        private HeatMapData heatMapData;
    }

    @Data
    @NoArgsConstructor
    public static class HeatMapData {
        
        @JsonProperty("result")
        private Map<String, Integer> result;
    }
}
