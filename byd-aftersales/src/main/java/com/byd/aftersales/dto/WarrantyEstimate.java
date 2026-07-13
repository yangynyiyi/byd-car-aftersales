package com.byd.aftersales.dto;

import java.math.BigDecimal;
import java.util.List;

public class WarrantyEstimate {

    private BigDecimal laborAmount;
    private BigDecimal partAmount;
    private BigDecimal grossAmount;
    private BigDecimal suggestedWarrantyAmount;
    private BigDecimal customerPayable;
    private boolean vehicleInWarranty;
    private String vehicleVin;
    private Integer vehicleAgeYears;
    private BigDecimal vehicleMileage;
    private List<String> notes;

    public BigDecimal getLaborAmount() { return laborAmount; }
    public void setLaborAmount(BigDecimal laborAmount) { this.laborAmount = laborAmount; }
    public BigDecimal getPartAmount() { return partAmount; }
    public void setPartAmount(BigDecimal partAmount) { this.partAmount = partAmount; }
    public BigDecimal getGrossAmount() { return grossAmount; }
    public void setGrossAmount(BigDecimal grossAmount) { this.grossAmount = grossAmount; }
    public BigDecimal getSuggestedWarrantyAmount() { return suggestedWarrantyAmount; }
    public void setSuggestedWarrantyAmount(BigDecimal suggestedWarrantyAmount) { this.suggestedWarrantyAmount = suggestedWarrantyAmount; }
    public BigDecimal getCustomerPayable() { return customerPayable; }
    public void setCustomerPayable(BigDecimal customerPayable) { this.customerPayable = customerPayable; }
    public boolean isVehicleInWarranty() { return vehicleInWarranty; }
    public void setVehicleInWarranty(boolean vehicleInWarranty) { this.vehicleInWarranty = vehicleInWarranty; }
    public String getVehicleVin() { return vehicleVin; }
    public void setVehicleVin(String vehicleVin) { this.vehicleVin = vehicleVin; }
    public Integer getVehicleAgeYears() { return vehicleAgeYears; }
    public void setVehicleAgeYears(Integer vehicleAgeYears) { this.vehicleAgeYears = vehicleAgeYears; }
    public BigDecimal getVehicleMileage() { return vehicleMileage; }
    public void setVehicleMileage(BigDecimal vehicleMileage) { this.vehicleMileage = vehicleMileage; }
    public List<String> getNotes() { return notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }
}
