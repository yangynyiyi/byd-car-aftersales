package com.byd.car.battery.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.car.battery.model.BatteryHealthRecord;
import com.byd.car.battery.service.BatteryHealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/battery")
public class BatteryHealthController {

    @Autowired
    private BatteryHealthService batteryHealthService;

    @PostMapping("/records")
    public ApiResponse<BatteryHealthRecord> addRecord(@RequestBody BatteryHealthRecord record) {
        BatteryHealthRecord saved = batteryHealthService.addRecord(record);
        return ApiResponse.ok(saved);
    }

    @GetMapping("/records/{vin}")
    public ApiResponse<List<BatteryHealthRecord>> listByVin(@PathVariable String vin) {
        List<BatteryHealthRecord> list = batteryHealthService.getAllByVin(vin);
        return ApiResponse.ok(list);
    }

    @GetMapping("/records/{vin}/latest")
    public ApiResponse<BatteryHealthRecord> getLatest(@PathVariable String vin) {
        BatteryHealthRecord record = batteryHealthService.getLatestByVin(vin);
        if (record == null) {
            return ApiResponse.fail(404, "该车辆暂无电池检测记录");
        }
        return ApiResponse.ok(record);
    }

    @GetMapping("/warnings")
    public ApiResponse<List<BatteryHealthRecord>> listWarnings() {
        List<BatteryHealthRecord> list = batteryHealthService.listWarningVehicles();
        return ApiResponse.ok(list);
    }
}
