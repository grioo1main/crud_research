package com.lum1nar.tcp_project.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MetricDataPoint {
    private LocalDateTime timestamp;
    private String httpMethod;
    private double avgResponseTimeMs;
    private long requestCount;
}