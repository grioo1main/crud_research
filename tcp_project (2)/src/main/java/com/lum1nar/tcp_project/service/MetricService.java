package com.lum1nar.tcp_project.service;

import com.lum1nar.tcp_project.dto.MetricDataPoint;
import com.lum1nar.tcp_project.dto.MetricsSummary;
import com.lum1nar.tcp_project.model.RequestMetric;
import com.lum1nar.tcp_project.repository.MetricRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricService {

        private final MetricRepository metricRepository;

        @Async
        public void saveMetric(RequestMetric metric) {
                metricRepository.save(metric);
        }

        public void saveMetricSync(RequestMetric metric) {
                metricRepository.save(metric);
        }
        // Добавьте в MetricService.java

        @Transactional
        public void deleteAllMetrics() {
                metricRepository.deleteAll();
        }

        @Transactional
        public void deleteLoadTestMetrics() {
                metricRepository.deleteByLoadTestTrue();

        }

        public MetricsSummary getSummary(int lastMinutes) {
                LocalDateTime after = LocalDateTime.now().minusMinutes(lastMinutes);
                List<RequestMetric> metrics = metricRepository.findNonLoadTestAfter(after);

                if (metrics.isEmpty()) {
                        return MetricsSummary.builder()
                                        .totalRequests(0)
                                        .avgResponseTimeMs(0)
                                        .medianResponseTimeMs(0)
                                        .p95ResponseTimeMs(0)
                                        .requestsPerSecond(0)
                                        .errorRate(0)
                                        .byMethod(Map.of())
                                        .build();
                }

                List<Long> allTimes = metrics.stream()
                                .map(RequestMetric::getResponseTimeMs)
                                .sorted()
                                .collect(Collectors.toList());

                double avg = allTimes.stream().mapToLong(Long::longValue).average().orElse(0);
                double median = percentile(allTimes, 50);
                double p95 = percentile(allTimes, 95);

                long errors = metrics.stream()
                                .filter(m -> m.getStatusCode() >= 400)
                                .count();

                double errorRate = (double) errors / metrics.size() * 100;

                long spanMs = java.time.Duration.between(
                                metrics.get(0).getTimestamp(),
                                metrics.get(metrics.size() - 1).getTimestamp()).toMillis();
                double rps = spanMs > 0 ? (double) metrics.size() / spanMs * 1000 : metrics.size();

                Map<String, MetricsSummary.MethodDetail> byMethod = metrics.stream()
                                .collect(Collectors.groupingBy(RequestMetric::getHttpMethod))
                                .entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> buildMethodDetail(e.getValue())));

                return MetricsSummary.builder()
                                .totalRequests(metrics.size())
                                .avgResponseTimeMs(round(avg))
                                .medianResponseTimeMs(round(median))
                                .p95ResponseTimeMs(round(p95))
                                .requestsPerSecond(round(rps))
                                .errorRate(round(errorRate))
                                .byMethod(byMethod)
                                .build();
        }

        public List<MetricDataPoint> getHistory(int lastMinutes, int bucketSeconds) {
                LocalDateTime after = LocalDateTime.now().minusMinutes(lastMinutes);
                List<RequestMetric> metrics = metricRepository.findNonLoadTestAfter(after);

                if (metrics.isEmpty())
                        return List.of();

                Map<String, Map<LocalDateTime, List<RequestMetric>>> grouped = metrics.stream()
                                .collect(Collectors.groupingBy(
                                                RequestMetric::getHttpMethod,
                                                Collectors.groupingBy(m -> m.getTimestamp()
                                                                .withSecond((m.getTimestamp().getSecond()
                                                                                / bucketSeconds) * bucketSeconds)
                                                                .withNano(0))));

                List<MetricDataPoint> result = new ArrayList<>();
                grouped.forEach((method,
                                buckets) -> buckets.forEach((time, list) -> result.add(MetricDataPoint.builder()
                                                .timestamp(time)
                                                .httpMethod(method)
                                                .avgResponseTimeMs(round(list.stream()
                                                                .mapToLong(RequestMetric::getResponseTimeMs)
                                                                .average().orElse(0)))
                                                .requestCount(list.size())
                                                .build())));

                result.sort(Comparator.comparing(MetricDataPoint::getTimestamp));
                return result;
        }

        public MetricsSummary getLoadTestSummary(String loadTestId) {
                List<RequestMetric> metrics = metricRepository.findByLoadTestIdOrdered(loadTestId);
                if (metrics.isEmpty()) {
                        return MetricsSummary.builder()
                                        .totalRequests(0)
                                        .byMethod(Map.of())
                                        .build();
                }

                List<Long> allTimes = metrics.stream()
                                .map(RequestMetric::getResponseTimeMs)
                                .sorted()
                                .collect(Collectors.toList());

                long errors = metrics.stream().filter(m -> m.getStatusCode() >= 400).count();
                long spanMs = java.time.Duration.between(
                                metrics.get(0).getTimestamp(),
                                metrics.get(metrics.size() - 1).getTimestamp()).toMillis();
                double rps = spanMs > 0 ? (double) metrics.size() / spanMs * 1000 : metrics.size();

                Map<String, MetricsSummary.MethodDetail> byMethod = metrics.stream()
                                .collect(Collectors.groupingBy(RequestMetric::getHttpMethod))
                                .entrySet().stream()
                                .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> buildMethodDetail(e.getValue())));

                return MetricsSummary.builder()
                                .totalRequests(metrics.size())
                                .avgResponseTimeMs(
                                                round(allTimes.stream().mapToLong(Long::longValue).average().orElse(0)))
                                .medianResponseTimeMs(round(percentile(allTimes, 50)))
                                .p95ResponseTimeMs(round(percentile(allTimes, 95)))
                                .requestsPerSecond(round(rps))
                                .errorRate(round((double) errors / metrics.size() * 100))
                                .byMethod(byMethod)
                                .build();
        }

        private MetricsSummary.MethodDetail buildMethodDetail(List<RequestMetric> metrics) {
                List<Long> times = metrics.stream()
                                .map(RequestMetric::getResponseTimeMs)
                                .sorted()
                                .collect(Collectors.toList());

                long errors = metrics.stream().filter(m -> m.getStatusCode() >= 400).count();

                return MetricsSummary.MethodDetail.builder()
                                .count(metrics.size())
                                .avgTimeMs(round(times.stream().mapToLong(Long::longValue).average().orElse(0)))
                                .medianTimeMs(round(percentile(times, 50)))
                                .p95TimeMs(round(percentile(times, 95)))
                                .minTimeMs(times.get(0))
                                .maxTimeMs(times.get(times.size() - 1))
                                .errors(errors)
                                .build();
        }

        private double percentile(List<Long> sorted, int p) {
                if (sorted.isEmpty())
                        return 0;
                int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
                return sorted.get(Math.max(0, index));
        }

        private double round(double value) {
                return Math.round(value * 100.0) / 100.0;
        }
}