package com.byd.aftersales.dto;

import java.util.List;
import java.util.Map;

public class DashboardStats {

    private long vehicleCount;
    private long appointmentPending;
    private long workOrderInProgress;
    private long batteryDangerCount;
    private long lowStockPartCount;
    private List<Map<String, Object>> technicianWorkload;

    public long getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(long vehicleCount) { this.vehicleCount = vehicleCount; }
    public long getAppointmentPending() { return appointmentPending; }
    public void setAppointmentPending(long appointmentPending) { this.appointmentPending = appointmentPending; }
    public long getWorkOrderInProgress() { return workOrderInProgress; }
    public void setWorkOrderInProgress(long workOrderInProgress) { this.workOrderInProgress = workOrderInProgress; }
    public long getBatteryDangerCount() { return batteryDangerCount; }
    public void setBatteryDangerCount(long batteryDangerCount) { this.batteryDangerCount = batteryDangerCount; }
    public long getLowStockPartCount() { return lowStockPartCount; }
    public void setLowStockPartCount(long lowStockPartCount) { this.lowStockPartCount = lowStockPartCount; }
    public List<Map<String, Object>> getTechnicianWorkload() { return technicianWorkload; }
    public void setTechnicianWorkload(List<Map<String, Object>> technicianWorkload) { this.technicianWorkload = technicianWorkload; }
}
