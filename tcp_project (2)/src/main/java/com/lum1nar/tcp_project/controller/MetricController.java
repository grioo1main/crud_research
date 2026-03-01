package com.lum1nar.tcp_project.controller;

import com.lum1nar.tcp_project.dto.MetricDataPoint;
import com.lum1nar.tcp_project.dto.MetricsSummary;
import com.lum1nar.tcp_project.service.MetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
public class MetricController {

    private final MetricService metricService;

    @GetMapping("/summary")
    public ResponseEntity<MetricsSummary> getSummary(
            @RequestParam(defaultValue = "60") int lastMinutes) {
        return ResponseEntity.ok(metricService.getSummary(lastMinutes));
    }

    @GetMapping("/history")
    public ResponseEntity<List<MetricDataPoint>> getHistory(
            @RequestParam(defaultValue = "60") int lastMinutes,
            @RequestParam(defaultValue = "10") int bucketSeconds) {
        return ResponseEntity.ok(metricService.getHistory(lastMinutes, bucketSeconds));
    }

    @GetMapping("/loadtest/{testId}")
    public ResponseEntity<MetricsSummary> getLoadTestSummary(@PathVariable String testId) {
        return ResponseEntity.ok(metricService.getLoadTestSummary(testId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllMetrics() {
        metricService.deleteAllMetrics();
        return ResponseEntity.ok("All metrics cleared");
    }

    @DeleteMapping("/clear/loadtest")
    public ResponseEntity<String> clearLoadTestMetrics() {
        metricService.deleteLoadTestMetrics();
        return ResponseEntity.ok("Load test metrics cleared");
    }
}