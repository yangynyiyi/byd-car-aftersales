package com.byd.car.workorder.controller;

import com.byd.car.common.Result;
import com.byd.car.settlement.entity.Settlement;
import com.byd.car.transaction.WorkOrderCompletionService;
import com.byd.car.workorder.dto.WorkOrderCompleteRequest;
import com.byd.car.workorder.dto.WorkOrderCreateRequest;
import com.byd.car.workorder.entity.WorkOrder;
import com.byd.car.workorder.service.WorkOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final WorkOrderCompletionService completionService;

    /** ADVISOR: 创建工单 */
    @PostMapping
    public Result<WorkOrder> create(@RequestBody WorkOrderCreateRequest request) {
        return Result.ok(workOrderService.create(request));
    }

    /** ADVISOR/TECHNICIAN: 查询工单详情 */
    @GetMapping("/{id}")
    public Result<WorkOrder> getById(@PathVariable Long id) {
        return Result.ok(workOrderService.getById(id));
    }

    /** ADVISOR: 工单列表 */
    @GetMapping
    public Result<List<WorkOrder>> listAll() {
        return Result.ok(workOrderService.listAll());
    }

    /** TECHNICIAN: 查询自己的工单 */
    @GetMapping("/my")
    public Result<List<WorkOrder>> listByTechnician(@RequestParam Long technicianId) {
        return Result.ok(workOrderService.listByTechnician(technicianId));
    }

    /** ADVISOR: 指派技师 */
    @PutMapping("/{id}/assign")
    public Result<WorkOrder> assign(@PathVariable Long id,
                                    @RequestBody Map<String, Long> body) {
        Long technicianId = body.get("technicianId");
        return Result.ok(workOrderService.assignTechnician(id, technicianId));
    }

    /** TECHNICIAN: 开始维修 */
    @PutMapping("/{id}/start")
    public Result<WorkOrder> start(@PathVariable Long id) {
        return Result.ok(workOrderService.startRepair(id));
    }

    /**
     * TECHNICIAN: 完工 — 触发事务
     * 原子执行：扣库存 → 标记备件已用 → 生成结算单 → 工单完工
     */
    @PostMapping("/{id}/complete")
    public Result<Settlement> complete(@PathVariable Long id,
                                       @RequestBody WorkOrderCompleteRequest request) {
        BigDecimal warranty = request.getWarrantyAmount() != null
                ? request.getWarrantyAmount() : BigDecimal.ZERO;
        Settlement settlement = completionService.complete(
                id, request.getRepairResult(), warranty, request.getOperatorId());
        return Result.ok(settlement);
    }
}
