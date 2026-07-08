package com.byd.aftersales.dto;

import java.math.BigDecimal;

public class WorkOrderCompleteRequest {

    private String repairResult;
    private BigDecimal warrantyAmount;
    private Long operatorId;

    public String getRepairResult() { return repairResult; }
    public void setRepairResult(String repairResult) { this.repairResult = repairResult; }
    public BigDecimal getWarrantyAmount() { return warrantyAmount; }
    public void setWarrantyAmount(BigDecimal warrantyAmount) { this.warrantyAmount = warrantyAmount; }
    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }
}
