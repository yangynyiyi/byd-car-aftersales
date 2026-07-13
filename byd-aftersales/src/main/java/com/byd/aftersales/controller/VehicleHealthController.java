package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.VehicleHealthSnapshot;
import com.byd.aftersales.service.VehicleHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-health")
public class VehicleHealthController {

    private final VehicleHealthService vehicleHealthService;

    public VehicleHealthController(VehicleHealthService vehicleHealthService) {
        this.vehicleHealthService = vehicleHealthService;
    }

    @GetMapping("/latest")
    public ApiResponse<List<VehicleHealthSnapshot>> latestByOwner(@RequestParam("ownerId") Long ownerId) {
        return ApiResponse.ok(vehicleHealthService.listLatestByOwner(ownerId));
    }

    @GetMapping("/vehicle/{vin}")
    public ApiResponse<List<VehicleHealthSnapshot>> historyByVehicle(@PathVariable("vin") String vin) {
        return ApiResponse.ok(vehicleHealthService.listByVin(vin));
    }

    @PostMapping("/snapshots")
    public ApiResponse<VehicleHealthSnapshot> createSnapshot(@RequestBody VehicleHealthSnapshot snapshot) {
        return ApiResponse.ok(vehicleHealthService.createSnapshot(snapshot));
    }
}
