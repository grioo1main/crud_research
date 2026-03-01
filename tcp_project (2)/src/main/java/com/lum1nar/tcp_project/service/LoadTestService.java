package com.lum1nar.tcp_project.service;

import com.lum1nar.tcp_project.dto.ItemRequest;
import com.lum1nar.tcp_project.dto.LoadTestRequest;
import com.lum1nar.tcp_project.dto.LoadTestResult;
import com.lum1nar.tcp_project.model.RequestMetric;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoadTestService {

    private final MetricService metricService;
    private final ItemService itemService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8080";
    
    private final AtomicLong deleteItemId = new AtomicLong(1);

    public LoadTestResult runLoadTest(LoadTestRequest request) {
        String testId = UUID.randomUUID().toString();
        int total = request.getTotalRequests();
        int threads = request.getThreads();
        List<String> methods = request.getMethods();

        log.info("Starting load test {} : {} requests, {} threads, methods: {}",
                testId, total, threads, methods);

        int itemsNeeded = total / methods.size() + 10;
        seedItems(itemsNeeded);
        
        deleteItemId.set(1);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<RequestMetric>> futures = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < total; i++) {
            String method = methods.get(i % methods.size());
            futures.add(executor.submit(() -> executeRequest(method, testId, counter.incrementAndGet())));
        }

        List<RequestMetric> results = new ArrayList<>();
        for (Future<RequestMetric> f : futures) {
            try {
                RequestMetric m = f.get(30, TimeUnit.SECONDS);
                if (m != null) {
                    results.add(m);
                }
            } catch (Exception e) {
                log.error("Load test request failed", e);
                results.add(RequestMetric.builder()
                        .httpMethod("ERROR")
                        .uri("/error")
                        .statusCode(500)
                        .responseTimeMs(0L)
                        .timestamp(LocalDateTime.now())
                        .loadTest(true)
                        .loadTestId(testId)
                        .build());
            }
        }

        executor.shutdown();
        long totalTime = System.currentTimeMillis() - startTime;

        results.forEach(metricService::saveMetricSync);

        return buildResult(testId, results, totalTime);
    }

    private RequestMetric executeRequest(String method, String testId, int index) {
        long start = System.currentTimeMillis();
        int statusCode = 200;
        String uri = "";

        try {
            switch (method.toUpperCase()) {
                case "GET" -> {
                    // GET только по ID - самый быстрый запрос!
                    long itemCount = itemService.count();
                    long id = itemCount > 0 ? (index % itemCount) + 1 : 1;
                    uri = "/api/items/" + id;
                    ResponseEntity<String> resp = restTemplate.getForEntity(BASE_URL + uri, String.class);
                    statusCode = resp.getStatusCode().value();
                }
                case "POST" -> {
                    uri = "/api/items";
                    ItemRequest body = new ItemRequest(
                            "LoadTest Item " + index,
                            "Generated during load test " + testId,
                            BigDecimal.valueOf(Math.random() * 1000).setScale(2, RoundingMode.HALF_UP)
                    );
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<ItemRequest> entity = new HttpEntity<>(body, headers);
                    ResponseEntity<String> resp = restTemplate.postForEntity(BASE_URL + uri, entity, String.class);
                    statusCode = resp.getStatusCode().value();
                }
                case "PUT" -> {
                    long itemCount = itemService.count();
                    long id = itemCount > 0 ? (index % itemCount) + 1 : 1;
                    uri = "/api/items/" + id;
                    ItemRequest body = new ItemRequest(
                            "Updated Item " + index,
                            "Updated during load test " + testId,
                            BigDecimal.valueOf(Math.random() * 500).setScale(2, RoundingMode.HALF_UP)
                    );
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<ItemRequest> entity = new HttpEntity<>(body, headers);
                    try {
                        restTemplate.exchange(BASE_URL + uri, HttpMethod.PUT, entity, String.class);
                    } catch (Exception e) {
                        statusCode = 404;
                    }
                }
                case "DELETE" -> {
                    long id = deleteItemId.getAndIncrement();
                    long maxId = itemService.count();
                    if (id > maxId) {
                        deleteItemId.set(1);
                        id = 1;
                    }
                    uri = "/api/items/" + id;
                    try {
                        restTemplate.delete(BASE_URL + uri);
                        statusCode = 204;
                    } catch (Exception e) {
                        statusCode = 404;
                    }
                }
            }
        } catch (Exception e) {
            statusCode = 500;
            log.warn("Request failed: {} {} - {}", method, uri, e.getMessage());
        }

        long elapsed = System.currentTimeMillis() - start;

        return RequestMetric.builder()
                .httpMethod(method.toUpperCase())
                .uri(uri)
                .statusCode(statusCode)
                .responseTimeMs(elapsed)
                .timestamp(LocalDateTime.now())
                .loadTest(true)
                .loadTestId(testId)
                .build();
    }

    private void seedItems(int count) {
        long existing = itemService.count();
        int toCreate = (int) Math.max(0, count - existing);
        for (int i = 0; i < toCreate; i++) {
            itemService.create(new ItemRequest(
                    "Seed Item " + (existing + i),
                    "Pre-seeded item for load testing",
                    BigDecimal.valueOf(10 + i)
            ));
        }
        log.info("Seeded {} items, total now: {}", toCreate, itemService.count());
    }

    private LoadTestResult buildResult(String testId, List<RequestMetric> metrics, long totalTime) {
        int success = (int) metrics.stream().filter(m -> m.getStatusCode() < 400).count();
        int failed = metrics.size() - success;

        Map<String, LoadTestResult.MethodStats> methodStats = metrics.stream()
                .filter(m -> !m.getHttpMethod().equals("ERROR"))
                .collect(Collectors.groupingBy(RequestMetric::getHttpMethod))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            List<Long> times = e.getValue().stream()
                                    .map(RequestMetric::getResponseTimeMs)
                                    .sorted()
                                    .collect(Collectors.toList());
                            int errs = (int) e.getValue().stream()
                                    .filter(m -> m.getStatusCode() >= 400).count();
                            return LoadTestResult.MethodStats.builder()
                                    .count(e.getValue().size())
                                    .avgTimeMs(round(times.stream().mapToLong(Long::longValue).average().orElse(0)))
                                    .medianTimeMs(round(percentile(times, 50)))
                                    .p95TimeMs(round(percentile(times, 95)))
                                    .minTimeMs(times.isEmpty() ? 0 : times.get(0))
                                    .maxTimeMs(times.isEmpty() ? 0 : times.get(times.size() - 1))
                                    .errors(errs)
                                    .build();
                        }
                ));

        double avgTime = metrics.stream()
                .mapToLong(RequestMetric::getResponseTimeMs)
                .average().orElse(0);

        return LoadTestResult.builder()
                .loadTestId(testId)
                .totalRequests(metrics.size())
                .successfulRequests(success)
                .failedRequests(failed)
                .totalTimeMs(totalTime)
                .avgResponseTimeMs(round(avgTime))
                .requestsPerSecond(round((double) metrics.size() / totalTime * 1000))
                .methodStats(methodStats)
                .build();
    }

    private double percentile(List<Long> sorted, int p) {
        if (sorted.isEmpty()) return 0;
        int index = (int) Math.ceil(p / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
