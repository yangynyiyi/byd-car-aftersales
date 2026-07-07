package com.byd.car.settlement.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Settlement {

    private Long settlementId;
    private String settlementNo;
    private Long workOrderId;
    private BigDecimal laborAmount;
    private BigDecimal partAmount;
    /** 质保抵扣金额 */
    private BigDecimal warrantyAmount;
    /** total = laborAmount + partAmount - warrantyAmount */
    private BigDecimal totalAmount;
    /**
     * UNPAID / PAID / REFUNDED
     */
    private String paymentStatus;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
