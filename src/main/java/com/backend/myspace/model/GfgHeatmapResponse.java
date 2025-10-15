package com.backend.myspace.model;

import java.util.Map;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) 
public class GfgHeatmapResponse {

    @JsonProperty("pageProps")
    private PageProps pageProps;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) 
    public static class PageProps {
        @JsonProperty("heatMapData")
        private HeatMapData heatMapData;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true) 
    public static class HeatMapData {
        @JsonProperty("result")
        private Map<String, Integer> result;
    }
}