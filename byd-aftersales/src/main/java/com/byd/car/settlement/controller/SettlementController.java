package com.byd.car.settlement.controller;

import com.byd.car.common.Result;
import com.byd.car.settlement.entity.Settlement;
import com.byd.car.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /** ADVISOR: 按工单查结算单 */
    @GetMapping("/work-order/{workOrderId}")
    public Result<Settlement> getByWorkOrder(@PathVariable Long workOrderId) {
        return Result.ok(settlementService.getByWorkOrderId(workOrderId));
    }

    /** ADVISOR: 按结算单 ID 查询 */
    @GetMapping("/{id}")
    public Result<Settlement> getById(@PathVariable Long id) {
        return Result.ok(settlementService.getById(id));
    }

    /** ADVISOR: 确认收款 */
    @PutMapping("/{id}/pay")
    public Result<Settlement> pay(@PathVariable Long id) {
        return Result.ok(settlementService.markPaid(id));
    }
}
