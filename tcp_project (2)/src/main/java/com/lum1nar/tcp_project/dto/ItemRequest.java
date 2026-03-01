package com.lum1nar.tcp_project.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
}