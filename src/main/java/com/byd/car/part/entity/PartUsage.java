package com.byd.car.part.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PartUsage {

    private Long usageId;
    private Long workOrderId;
    private Long partId;
    private Integer quantity;
    /** 申请时快照备件售价 */
    private BigDecimal unitPrice;
    private Long technicianId;
    private Long approvedBy;
    /**
     * APPLIED / APPROVED / REJECTED / USED / RETURNED
     */
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
}
