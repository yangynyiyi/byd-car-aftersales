package com.byd.car.workorder.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WorkOrder {

    private Long workOrderId;
    private String workOrderNo;
    private Long faultId;
    private Long diagnosisId;
    private Long technicianId;
    /**
     * CREATED / ASSIGNED / IN_PROGRESS / PART_WAITING / COMPLETED / CANCELLED
     */
    private String status;
    private BigDecimal laborCost;
    private String repairResult;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
