package com.byd.car.agent.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.car.agent.model.AgentDiagnosis;
import com.byd.car.agent.model.AgentDiagnosisResult;
import com.byd.car.agent.service.AgentDiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-diagnosis")
public class AgentDiagnosisController {

    @Autowired
    private AgentDiagnosisService agentDiagnosisService;

    @PostMapping("/trigger")
    public ApiResponse<AgentDiagnosisResult> triggerDiagnosis(@RequestParam Long faultId) {
        AgentDiagnosisResult result = agentDiagnosisService.diagnose(faultId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/trigger")
    public ApiResponse<AgentDiagnosisResult> triggerDiagnosisGet(@RequestParam Long faultId) {
        AgentDiagnosisResult result = agentDiagnosisService.diagnose(faultId);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{diagnosisId}")
    public ApiResponse<AgentDiagnosis> getById(@PathVariable Long diagnosisId) {
        AgentDiagnosis diagnosis = agentDiagnosisService.getById(diagnosisId);
        if (diagnosis == null) {
            return ApiResponse.fail(404, "诊断记录不存在");
        }
        return ApiResponse.ok(diagnosis);
    }

    @GetMapping("/list")
    public ApiResponse<List<AgentDiagnosis>> listByVin(@RequestParam String vin) {
        List<AgentDiagnosis> list = agentDiagnosisService.getByVin(vin);
        return ApiResponse.ok(list);
    }

    @GetMapping("/by-fault/{faultId}")
    public ApiResponse<List<AgentDiagnosis>> listByFaultId(@PathVariable Long faultId) {
        List<AgentDiagnosis> list = agentDiagnosisService.getByFaultId(faultId);
        return ApiResponse.ok(list);
    }
}
