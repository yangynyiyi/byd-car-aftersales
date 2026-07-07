package com.byd.car.part.controller;

import com.byd.car.common.Result;
import com.byd.car.part.dto.PartUsageCreateRequest;
import com.byd.car.part.entity.PartUsage;
import com.byd.car.part.service.PartUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/part-usages")
@RequiredArgsConstructor
public class PartUsageController {

    private final PartUsageService partUsageService;

    /** TECHNICIAN: 申请备件 */
    @PostMapping
    public Result<PartUsage> apply(@RequestBody PartUsageCreateRequest request) {
        return Result.ok(partUsageService.apply(request));
    }

    /** 查看工单的备件申请列表 */
    @GetMapping
    public Result<List<PartUsage>> listByWorkOrder(@RequestParam Long workOrderId) {
        return Result.ok(partUsageService.listByWorkOrder(workOrderId));
    }

    /** PART_ADMIN: 审批通过 */
    @PutMapping("/{id}/approve")
    public Result<PartUsage> approve(@PathVariable Long id,
                                     @RequestBody Map<String, Long> body) {
        Long approvedBy = body.get("approvedBy");
        return Result.ok(partUsageService.approve(id, approvedBy));
    }

    /** PART_ADMIN: 审批拒绝 */
    @PutMapping("/{id}/reject")
    public Result<PartUsage> reject(@PathVariable Long id,
                                    @RequestBody Map<String, Long> body) {
        Long approvedBy = body.get("approvedBy");
        return Result.ok(partUsageService.reject(id, approvedBy));
    }
}
