package com.byd.aftersales.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VehicleHealthSnapshot {

    private Long snapshotId;
    private String vin;
    private Integer healthScore;
    private String overallLevel;
    private String summary;
    private String suggestion;
    private LocalDateTime detectTime;
    private LocalDateTime createdAt;
    private Vehicle vehicle;
    private List<VehicleHealthItem> items = new ArrayList<>();

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId) {
        this.snapshotId = snapshotId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Integer getHealthScore() {
        return healthScore;
    }

    public void setHealthScore(Integer healthScore) {
        this.healthScore = healthScore;
    }

    public String getOverallLevel() {
        return overallLevel;
    }

    public void setOverallLevel(String overallLevel) {
        this.overallLevel = overallLevel;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public LocalDateTime getDetectTime() {
        return detectTime;
    }

    public void setDetectTime(LocalDateTime detectTime) {
        this.detectTime = detectTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public List<VehicleHealthItem> getItems() {
        return items;
    }

    public void setItems(List<VehicleHealthItem> items) {
        this.items = items;
    }
}
