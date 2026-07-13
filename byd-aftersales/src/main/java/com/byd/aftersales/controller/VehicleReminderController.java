package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.VehicleReminder;
import com.byd.aftersales.service.VehicleReminderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class VehicleReminderController {

    private final VehicleReminderService reminderService;

    public VehicleReminderController(VehicleReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public ApiResponse<List<VehicleReminder>> listByOwner(@RequestParam("ownerId") Long ownerId) {
        return ApiResponse.ok(reminderService.listByOwner(ownerId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable("id") Long id, @RequestParam("ownerId") Long ownerId) {
        reminderService.markRead(id, ownerId);
        return ApiResponse.ok();
    }
}
