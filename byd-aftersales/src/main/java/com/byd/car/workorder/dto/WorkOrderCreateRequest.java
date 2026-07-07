package com.byd.car.workorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkOrderCreateRequest {

    private Long faultId;
    private Long diagnosisId;
    private Long technicianId;
    private BigDecimal laborCost;
}
