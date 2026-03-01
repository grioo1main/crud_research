package com.lum1nar.tcp_project.dto;

import lombok.*;
import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoadTestResult {
    private String loadTestId;
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private long totalTimeMs;
    private double avgResponseTimeMs;
    private double requestsPerSecond;
    private Map<String, MethodStats> methodStats;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class MethodStats {
        private int count;
        private double avgTimeMs;
        private double medianTimeMs;
        private double p95TimeMs;
        private long minTimeMs;
        private long maxTimeMs;
        private int errors;
    }
}
