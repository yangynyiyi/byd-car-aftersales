package com.byd.car.part.dto;

import lombok.Data;

@Data
public class PartUsageCreateRequest {

    private Long workOrderId;
    private Long partId;
    private Integer quantity;
    /** 申请技师 ID */
    private Long technicianId;
}
