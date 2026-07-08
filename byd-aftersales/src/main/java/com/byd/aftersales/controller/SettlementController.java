package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.Settlement;
import com.byd.aftersales.service.SettlementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public ApiResponse<List<Settlement>> listAll() {
        return ApiResponse.ok(settlementService.listAll());
    }

    @GetMapping("/work-order/{workOrderId}")
    public ApiResponse<Settlement> getByWorkOrder(@PathVariable Long workOrderId) {
        return ApiResponse.ok(settlementService.getByWorkOrderId(workOrderId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Settlement> getById(@PathVariable Long id) {
        return ApiResponse.ok(settlementService.getById(id));
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<Settlement> pay(@PathVariable Long id) {
        return ApiResponse.ok(settlementService.markPaid(id));
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<Settlement> approve(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return ApiResponse.ok(settlementService.approve(id, body.get("operatorId")));
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Settlement> reject(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        return ApiResponse.ok(settlementService.reject(id, body.get("operatorId")));
    }
}
