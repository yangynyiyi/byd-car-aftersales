package com.byd.aftersales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BatteryHealthRecord {

    private Long batteryRecordId;
    private String vin;
    private BigDecimal soh;
    private Integer chargeCycles;
    private BigDecimal maxTemperature;
    private BigDecimal minTemperature;
    private BigDecimal voltageDiff;
    private String warningLevel;
    private LocalDateTime detectTime;
    private LocalDateTime createdAt;

    public Long getBatteryRecordId() { return batteryRecordId; }
    public void setBatteryRecordId(Long batteryRecordId) { this.batteryRecordId = batteryRecordId; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public BigDecimal getSoh() { return soh; }
    public void setSoh(BigDecimal soh) { this.soh = soh; }
    public Integer getChargeCycles() { return chargeCycles; }
    public void setChargeCycles(Integer chargeCycles) { this.chargeCycles = chargeCycles; }
    public BigDecimal getMaxTemperature() { return maxTemperature; }
    public void setMaxTemperature(BigDecimal maxTemperature) { this.maxTemperature = maxTemperature; }
    public BigDecimal getMinTemperature() { return minTemperature; }
    public void setMinTemperature(BigDecimal minTemperature) { this.minTemperature = minTemperature; }
    public BigDecimal getVoltageDiff() { return voltageDiff; }
    public void setVoltageDiff(BigDecimal voltageDiff) { this.voltageDiff = voltageDiff; }
    public String getWarningLevel() { return warningLevel; }
    public void setWarningLevel(String warningLevel) { this.warningLevel = warningLevel; }
    public LocalDateTime getDetectTime() { return detectTime; }
    public void setDetectTime(LocalDateTime detectTime) { this.detectTime = detectTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
