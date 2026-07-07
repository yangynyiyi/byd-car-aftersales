package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.Appointment;
import com.byd.aftersales.service.AppointmentService;
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
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody Appointment appointment) {
        appointmentService.create(appointment);
        return ApiResponse.ok();
    }

    @PutMapping("/{appointmentNo}/status")
    public ApiResponse<Void> updateStatus(@PathVariable String appointmentNo, @RequestParam String status) {
        appointmentService.updateStatus(appointmentNo, status);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{appointmentNo}")
    public ApiResponse<Void> delete(@PathVariable String appointmentNo) {
        appointmentService.delete(appointmentNo);
        return ApiResponse.ok();
    }

    @GetMapping("/{appointmentNo}")
    public ApiResponse<Appointment> findByNo(@PathVariable String appointmentNo) {
        return ApiResponse.ok(appointmentService.findByNo(appointmentNo));
    }

    @GetMapping("/vehicle/{vin}")
    public ApiResponse<List<Appointment>> findByVin(@PathVariable String vin) {
        return ApiResponse.ok(appointmentService.findByVin(vin));
    }

    @GetMapping
    public ApiResponse<List<Appointment>> findAll() {
        return ApiResponse.ok(appointmentService.findAll());
    }
}
