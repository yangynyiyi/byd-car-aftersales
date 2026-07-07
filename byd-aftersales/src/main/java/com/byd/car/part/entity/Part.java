package com.byd.car.part.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Part {

    private Long partId;
    private String partNo;
    private String partName;
    private String category;
    private Integer stockQuantity;
    private Integer warningThreshold;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    /**
     * ENABLED / DISABLED
     */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
