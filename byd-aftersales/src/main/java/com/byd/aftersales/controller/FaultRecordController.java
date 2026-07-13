package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.FaultRecord;
import com.byd.aftersales.service.FaultRecordService;
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
@RequestMapping("/api/fault-records")
public class FaultRecordController {

    private final FaultRecordService faultRecordService;

    public FaultRecordController(FaultRecordService faultRecordService) {
        this.faultRecordService = faultRecordService;
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody FaultRecord record) {
        faultRecordService.create(record);
        return ApiResponse.ok();
    }

    @PutMapping("/{faultNo}/status")
    public ApiResponse<Void> updateStatus(@PathVariable("faultNo") String faultNo, @RequestParam("status") String status) {
        faultRecordService.updateStatus(faultNo, status);
        return ApiResponse.ok();
    }

    @PutMapping("/{faultNo}")
    public ApiResponse<Void> update(@PathVariable("faultNo") String faultNo, @RequestBody FaultRecord record) {
        faultRecordService.update(faultNo, record);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{faultNo}")
    public ApiResponse<Void> delete(@PathVariable("faultNo") String faultNo) {
        faultRecordService.delete(faultNo);
        return ApiResponse.ok();
    }

    @GetMapping("/{faultNo}")
    public ApiResponse<FaultRecord> findByNo(@PathVariable("faultNo") String faultNo) {
        return ApiResponse.ok(faultRecordService.findByNo(faultNo));
    }

    @GetMapping("/vehicle/{vin}")
    public ApiResponse<List<FaultRecord>> findByVin(@PathVariable("vin") String vin) {
        return ApiResponse.ok(faultRecordService.findByVin(vin));
    }

    @GetMapping
    public ApiResponse<List<FaultRecord>> findAll() {
        return ApiResponse.ok(faultRecordService.findAll());
    }
}
