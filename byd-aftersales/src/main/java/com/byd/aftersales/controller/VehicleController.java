package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.service.VehicleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Vehicle vehicle) {
        vehicleService.create(vehicle);
        return ApiResponse.ok();
    }

    @PutMapping("/{vin}")
    public ApiResponse<Void> update(@PathVariable String vin, @RequestBody Vehicle vehicle) {
        vehicleService.update(vin, vehicle);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{vin}")
    public ApiResponse<Void> delete(@PathVariable String vin) {
        vehicleService.delete(vin);
        return ApiResponse.ok();
    }

    @GetMapping("/{vin}")
    public ApiResponse<Vehicle> findByVin(@PathVariable String vin) {
        return ApiResponse.ok(vehicleService.findByVin(vin));
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Vehicle>> findByOwnerId(@PathVariable Long ownerId) {
        return ApiResponse.ok(vehicleService.findByOwnerId(ownerId));
    }

    @GetMapping
    public ApiResponse<List<Vehicle>> findAll() {
        return ApiResponse.ok(vehicleService.findAll());
    }
}
