package com.byd.car.workorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkOrderCompleteRequest {

    private String repairResult;
    /** 质保抵扣金额，默认 0 */
    private BigDecimal warrantyAmount;
    /** 操作人 ID（技师） */
    private Long operatorId;
}
