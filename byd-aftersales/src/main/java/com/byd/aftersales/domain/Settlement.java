package com.byd.aftersales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Settlement {

    private Long settlementId;
    private String settlementNo;
    private Long workOrderId;
    private BigDecimal laborAmount;
    private BigDecimal partAmount;
    private BigDecimal warrantyAmount;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String managerStatus;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }
    public String getSettlementNo() { return settlementNo; }
    public void setSettlementNo(String settlementNo) { this.settlementNo = settlementNo; }
    public Long getWorkOrderId() { return workOrderId; }
    public void setWorkOrderId(Long workOrderId) { this.workOrderId = workOrderId; }
    public BigDecimal getLaborAmount() { return laborAmount; }
    public void setLaborAmount(BigDecimal laborAmount) { this.laborAmount = laborAmount; }
    public BigDecimal getPartAmount() { return partAmount; }
    public void setPartAmount(BigDecimal partAmount) { this.partAmount = partAmount; }
    public BigDecimal getWarrantyAmount() { return warrantyAmount; }
    public void setWarrantyAmount(BigDecimal warrantyAmount) { this.warrantyAmount = warrantyAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getManagerStatus() { return managerStatus; }
    public void setManagerStatus(String managerStatus) { this.managerStatus = managerStatus; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
