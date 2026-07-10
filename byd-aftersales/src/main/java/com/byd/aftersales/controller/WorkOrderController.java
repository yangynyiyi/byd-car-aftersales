package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.Settlement;
import com.byd.aftersales.domain.WorkOrder;
import com.byd.aftersales.dto.WarrantyEstimate;
import com.byd.aftersales.dto.WorkOrderCompleteRequest;
import com.byd.aftersales.dto.WorkOrderCreateRequest;
import com.byd.aftersales.service.WarrantyService;
import com.byd.aftersales.service.WorkOrderCompletionService;
import com.byd.aftersales.service.WorkOrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderCompletionService completionService;
    private final WarrantyService warrantyService;

    public WorkOrderController(WorkOrderService workOrderService,
                               WorkOrderCompletionService completionService,
                               WarrantyService warrantyService) {
        this.workOrderService = workOrderService;
        this.completionService = completionService;
        this.warrantyService = warrantyService;
    }

    @PostMapping
    public ApiResponse<WorkOrder> create(@RequestBody WorkOrderCreateRequest request) {
        return ApiResponse.ok(workOrderService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrder> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(workOrderService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<WorkOrder>> listAll() {
        return ApiResponse.ok(workOrderService.listAll());
    }

    @GetMapping("/my")
    public ApiResponse<List<WorkOrder>> listByTechnician(@RequestParam("technicianId") Long technicianId) {
        return ApiResponse.ok(workOrderService.listByTechnician(technicianId));
    }

    @GetMapping("/supervisions")
    public ApiResponse<List<Map<String, Object>>> listSupervisions(@RequestParam("technicianId") Long technicianId) {
        return ApiResponse.ok(workOrderService.listSupervisionsForTechnician(technicianId));
    }

    @PutMapping("/{id}/assign")
    public ApiResponse<WorkOrder> assign(@PathVariable("id") Long id, @RequestBody Map<String, Long> body) {
        return ApiResponse.ok(workOrderService.assignTechnician(id, body.get("technicianId")));
    }

    @PutMapping("/{id}/start")
    public ApiResponse<WorkOrder> start(@PathVariable("id") Long id) {
        return ApiResponse.ok(workOrderService.startRepair(id));
    }

    @PutMapping("/{id}/part-waiting")
    public ApiResponse<WorkOrder> markPartWaiting(@PathVariable("id") Long id) {
        return ApiResponse.ok(workOrderService.markPartWaiting(id));
    }

    @GetMapping("/{id}/warranty-estimate")
    public ApiResponse<WarrantyEstimate> warrantyEstimate(@PathVariable("id") Long id) {
        return ApiResponse.ok(warrantyService.estimateForWorkOrder(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Settlement> complete(@PathVariable("id") Long id,
                                            @RequestBody WorkOrderCompleteRequest request) {
        BigDecimal warranty = request.getWarrantyAmount() != null
                ? request.getWarrantyAmount() : BigDecimal.ZERO;
        return ApiResponse.ok(completionService.complete(
                id, request.getRepairResult(), warranty, request.getOperatorId()));
    }

    @PutMapping("/{id}/supervise")
    public ApiResponse<Void> supervise(@PathVariable("id") Long id, @RequestBody Map<String, Long> body) {
        workOrderService.supervise(id, body.get("operatorId"));
        return ApiResponse.ok();
    }
}
