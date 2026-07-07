package com.byd.battery.controller;

import com.byd.battery.model.BatteryHealthRecord;
import com.byd.battery.service.BatteryHealthService;
import com.byd.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/battery")
public class BatteryHealthController {

    @Autowired
    private BatteryHealthService batteryHealthService;

    @PostMapping("/records")
    public Result<BatteryHealthRecord> addRecord(@RequestBody BatteryHealthRecord record) {
        BatteryHealthRecord saved = batteryHealthService.addRecord(record);
        return Result.ok(saved);
    }

    @GetMapping("/records/{vin}")
    public Result<List<BatteryHealthRecord>> listByVin(@PathVariable String vin) {
        List<BatteryHealthRecord> list = batteryHealthService.getAllByVin(vin);
        return Result.ok(list);
    }

    @GetMapping("/records/{vin}/latest")
    public Result<BatteryHealthRecord> getLatest(@PathVariable String vin) {
        BatteryHealthRecord record = batteryHealthService.getLatestByVin(vin);
        if (record == null) {
            return Result.error("该车辆暂无电池检测记录");
        }
        return Result.ok(record);
    }

    @GetMapping("/warnings")
    public Result<List<BatteryHealthRecord>> listWarnings() {
        List<BatteryHealthRecord> list = batteryHealthService.listWarningVehicles();
        return Result.ok(list);
    }
}
