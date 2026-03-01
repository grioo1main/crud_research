package com.lum1nar.tcp_project.controller;

import com.lum1nar.tcp_project.dto.LoadTestRequest;
import com.lum1nar.tcp_project.dto.LoadTestResult;
import com.lum1nar.tcp_project.service.LoadTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loadtest")
@RequiredArgsConstructor
public class LoadTestController {

    private final LoadTestService loadTestService;

    @PostMapping("/run")
    public ResponseEntity<LoadTestResult> runLoadTest(@RequestBody LoadTestRequest request) {
        return ResponseEntity.ok(loadTestService.runLoadTest(request));
    }
}