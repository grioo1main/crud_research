package com.lum1nar.tcp_project.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_metrics", indexes = {
    @Index(name = "idx_method", columnList = "httpMethod"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_load_test", columnList = "loadTest"),
    @Index(name = "idx_load_test_id", columnList = "loadTestId")
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RequestMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String httpMethod;

    @Column(nullable = false, length = 500)
    private String uri;

    @Column(nullable = false)
    private Integer statusCode;

    @Column(nullable = false)
    private Long responseTimeMs;

    private Long responseSizeBytes;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "load_test")
    private Boolean loadTest;

    @Column(name = "load_test_id")
    private String loadTestId;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (loadTest == null) {
            loadTest = false;
        }
    }
}