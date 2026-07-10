package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.Part;
import com.byd.aftersales.dto.PartCreateRequest;
import com.byd.aftersales.service.PartService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parts")
public class PartController {

    private final PartService partService;

    public PartController(PartService partService) {
        this.partService = partService;
    }

    @PostMapping
    public ApiResponse<Part> create(@RequestBody PartCreateRequest request) {
        return ApiResponse.ok(partService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Part> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(partService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Part>> listAll() {
        return ApiResponse.ok(partService.listAll());
    }

    @PutMapping("/{id}")
    public ApiResponse<Part> update(@PathVariable("id") Long id, @RequestBody PartCreateRequest request) {
        return ApiResponse.ok(partService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        partService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/stock")
    public ApiResponse<Part> addStock(@PathVariable("id") Long id, @RequestBody Map<String, Integer> body) {
        return ApiResponse.ok(partService.addStock(id, body.get("quantity")));
    }

    @GetMapping("/alerts")
    public ApiResponse<List<String>> getLowStockAlerts() {
        return ApiResponse.ok(partService.getLowStockAlerts());
    }
}
