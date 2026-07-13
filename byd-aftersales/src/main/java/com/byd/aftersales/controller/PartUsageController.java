package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.PartUsage;
import com.byd.aftersales.dto.PartUsageCreateRequest;
import com.byd.aftersales.service.PartUsageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/part-usages")
public class PartUsageController {

    private final PartUsageService partUsageService;

    public PartUsageController(PartUsageService partUsageService) {
        this.partUsageService = partUsageService;
    }

    @PostMapping
    public ApiResponse<PartUsage> apply(@RequestBody PartUsageCreateRequest request) {
        return ApiResponse.ok(partUsageService.apply(request));
    }

    @GetMapping
    public ApiResponse<List<PartUsage>> list(
            @RequestParam(value = "workOrderId", required = false) Long workOrderId,
            @RequestParam(value = "status", required = false) String status) {
        if (workOrderId != null) {
            return ApiResponse.ok(partUsageService.listByWorkOrder(workOrderId));
        }
        return ApiResponse.ok(partUsageService.listPending());
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<PartUsage> approve(@PathVariable("id") Long id, @RequestBody Map<String, Long> body) {
        return ApiResponse.ok(partUsageService.approve(id, body.get("approvedBy")));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<PartUsage> reject(@PathVariable("id") Long id, @RequestBody Map<String, Long> body) {
        return ApiResponse.ok(partUsageService.reject(id, body.get("approvedBy")));
    }
}
