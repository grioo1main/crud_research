package com.lum1nar.tcp_project.dto;

import lombok.*;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class LoadTestRequest {
    private int totalRequests = 100;
    private int threads = 10;
    private List<String> methods = List.of("GET", "POST", "PUT", "DELETE");
}