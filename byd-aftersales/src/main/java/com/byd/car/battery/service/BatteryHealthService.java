package com.byd.car.battery.service;

import com.byd.car.battery.model.BatteryHealthRecord;

import java.util.List;

public interface BatteryHealthService {

    String evaluateWarningLevel(BatteryHealthRecord record);

    BatteryHealthRecord addRecord(BatteryHealthRecord record);

    BatteryHealthRecord getLatestByVin(String vin);

    List<BatteryHealthRecord> getAllByVin(String vin);

    List<BatteryHealthRecord> listWarningVehicles();
}
