package com.lum1nar.tcp_project.dto;

import lombok.*;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MetricsSummary {
    private long totalRequests;
    private double avgResponseTimeMs;
    private double medianResponseTimeMs;
    private double p95ResponseTimeMs;
    private double requestsPerSecond;
    private double errorRate;
    private Map<String, MethodDetail> byMethod;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class MethodDetail {
        private long count;
        private double avgTimeMs;
        private double medianTimeMs;
        private double p95TimeMs;
        private long minTimeMs;
        private long maxTimeMs;
        private long errors;
    }
}