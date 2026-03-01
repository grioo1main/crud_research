package com.lum1nar.tcp_project.interceptor;

import com.lum1nar.tcp_project.model.RequestMetric;
import com.lum1nar.tcp_project.service.MetricService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MetricsInterceptor implements HandlerInterceptor {

    private final MetricService metricService;
    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        String uri = request.getRequestURI();

        // skip metric/actuator endpoints to avoid recursion
        if (uri.startsWith("/api/metrics") ||
            uri.startsWith("/api/loadtest") ||
            uri.startsWith("/actuator")) {
            return;
        }

        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) return;

        long elapsed = System.currentTimeMillis() - startTime;

        RequestMetric metric = RequestMetric.builder()
                .httpMethod(request.getMethod())
                .uri(uri)
                .statusCode(response.getStatus())
                .responseTimeMs(elapsed)
                .timestamp(LocalDateTime.now())
                .loadTest(false)
                .build();

        metricService.saveMetric(metric);
    }
}