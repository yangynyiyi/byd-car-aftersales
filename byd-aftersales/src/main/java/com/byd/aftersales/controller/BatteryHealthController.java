package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.service.BatteryHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/battery-alerts")
public class BatteryHealthController {

    private final BatteryHealthService batteryHealthService;

    public BatteryHealthController(BatteryHealthService batteryHealthService) {
        this.batteryHealthService = batteryHealthService;
    }

    @GetMapping
    public ApiResponse<List<BatteryHealthRecord>> list(@RequestParam(required = false) String level) {
        return ApiResponse.ok(batteryHealthService.list(level));
    }

    @GetMapping("/vehicle/{vin}")
    public ApiResponse<List<BatteryHealthRecord>> listByVin(@PathVariable String vin) {
        return ApiResponse.ok(batteryHealthService.listByVin(vin));
    }

    @PostMapping
    public ApiResponse<BatteryHealthRecord> create(@RequestBody BatteryHealthRecord record) {
        return ApiResponse.ok(batteryHealthService.create(record));
    }

    @PostMapping("/{vin}/remind")
    public ApiResponse<Void> remindOwner(@PathVariable String vin, @RequestBody Map<String, Long> body) {
        batteryHealthService.remindOwner(vin, body.get("operatorId"));
        return ApiResponse.ok();
    }
}
