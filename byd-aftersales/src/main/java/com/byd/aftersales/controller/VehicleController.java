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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResponse<Void> create(
            @RequestBody Vehicle vehicle,
            @RequestParam(value = "advisorId", required = false) Long advisorId) {
        vehicleService.create(vehicle, advisorId);
        return ApiResponse.ok();
    }

    @PutMapping("/{vin}")
    public ApiResponse<Void> update(
            @PathVariable("vin") String vin,
            @RequestBody Vehicle vehicle,
            @RequestParam(value = "advisorId", required = false) Long advisorId) {
        vehicleService.update(vin, vehicle, advisorId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{vin}")
    public ApiResponse<Void> delete(
            @PathVariable("vin") String vin,
            @RequestParam(value = "advisorId", required = false) Long advisorId) {
        vehicleService.delete(vin, advisorId);
        return ApiResponse.ok();
    }

    @GetMapping("/{vin}")
    public ApiResponse<Vehicle> findByVin(
            @PathVariable("vin") String vin,
            @RequestParam(value = "advisorId", required = false) Long advisorId) {
        return ApiResponse.ok(vehicleService.findByVin(vin, advisorId));
    }

    @GetMapping("/owner/{ownerId}")
    public ApiResponse<List<Vehicle>> findByOwnerId(@PathVariable("ownerId") Long ownerId) {
        return ApiResponse.ok(vehicleService.findByOwnerId(ownerId));
    }

    @GetMapping
    public ApiResponse<List<Vehicle>> list(
            @RequestParam(value = "advisorId", required = false) Long advisorId) {
        return ApiResponse.ok(vehicleService.list(advisorId));
    }
}
