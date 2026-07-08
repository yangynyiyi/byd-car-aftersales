package com.byd.aftersales.dto;

public class AgentDiagnoseRequest {

    private Long faultId;
    private String vin;
    private String faultDesc;

    public Long getFaultId() { return faultId; }
    public void setFaultId(Long faultId) { this.faultId = faultId; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getFaultDesc() { return faultDesc; }
    public void setFaultDesc(String faultDesc) { this.faultDesc = faultDesc; }
}
