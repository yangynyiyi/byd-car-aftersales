package com.byd.car.part.controller;

import com.byd.car.common.Result;
import com.byd.car.part.dto.PartCreateRequest;
import com.byd.car.part.entity.Part;
import com.byd.car.part.service.PartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    /** PART_ADMIN: 新增备件 */
    @PostMapping
    public Result<Part> create(@RequestBody PartCreateRequest request) {
        return Result.ok(partService.create(request));
    }

    /** 查询备件详情 */
    @GetMapping("/{id}")
    public Result<Part> getById(@PathVariable Long id) {
        return Result.ok(partService.getById(id));
    }

    /** 备件列表 */
    @GetMapping
    public Result<List<Part>> listAll() {
        return Result.ok(partService.listAll());
    }

    /** PART_ADMIN: 更新备件信息 */
    @PutMapping("/{id}")
    public Result<Part> update(@PathVariable Long id,
                               @RequestBody PartCreateRequest request) {
        return Result.ok(partService.update(id, request));
    }

    /** PART_ADMIN: 删除备件 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        partService.delete(id);
        return Result.ok();
    }

    /** PART_ADMIN: 补货 */
    @PostMapping("/{id}/stock")
    public Result<Part> addStock(@PathVariable Long id,
                                 @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        return Result.ok(partService.addStock(id, quantity));
    }

    /** PART_ADMIN: 低库存预警列表（读 Redis） */
    @GetMapping("/alerts")
    public Result<List<String>> getLowStockAlerts() {
        return Result.ok(partService.getLowStockAlerts());
    }
}
