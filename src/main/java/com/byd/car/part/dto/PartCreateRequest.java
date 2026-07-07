package com.byd.car.part.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PartCreateRequest {

    private String partNo;
    private String partName;
    private String category;
    private Integer stockQuantity;
    private Integer warningThreshold;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
}
