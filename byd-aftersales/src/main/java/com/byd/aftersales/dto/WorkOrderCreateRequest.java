package com.byd.aftersales.dto;

import java.math.BigDecimal;

public class WorkOrderCreateRequest {

    private Long faultId;
    private Long diagnosisId;
    private Long technicianId;
    private BigDecimal laborCost;

    public Long getFaultId() { return faultId; }
    public void setFaultId(Long faultId) { this.faultId = faultId; }
    public Long getDiagnosisId() { return diagnosisId; }
    public void setDiagnosisId(Long diagnosisId) { this.diagnosisId = diagnosisId; }
    public Long getTechnicianId() { return technicianId; }
    public void setTechnicianId(Long technicianId) { this.technicianId = technicianId; }
    public BigDecimal getLaborCost() { return laborCost; }
    public void setLaborCost(BigDecimal laborCost) { this.laborCost = laborCost; }
}
