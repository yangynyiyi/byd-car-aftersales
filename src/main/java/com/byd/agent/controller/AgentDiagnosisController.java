package com.byd.agent.controller;

import com.byd.agent.model.AgentDiagnosis;
import com.byd.agent.model.AgentDiagnosisResult;
import com.byd.agent.service.AgentDiagnosisService;
import com.byd.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent-diagnosis")
public class AgentDiagnosisController {

    @Autowired
    private AgentDiagnosisService agentDiagnosisService;

    @PostMapping("/trigger")
    public Result<AgentDiagnosisResult> triggerDiagnosis(@RequestParam Long faultId) {
        AgentDiagnosisResult result = agentDiagnosisService.diagnose(faultId);
        return Result.ok(result);
    }

    @GetMapping("/trigger")
    public Result<AgentDiagnosisResult> triggerDiagnosisGet(@RequestParam Long faultId) {
        AgentDiagnosisResult result = agentDiagnosisService.diagnose(faultId);
        return Result.ok(result);
    }

    @GetMapping("/{diagnosisId}")
    public Result<AgentDiagnosis> getById(@PathVariable Long diagnosisId) {
        AgentDiagnosis diagnosis = agentDiagnosisService.getById(diagnosisId);
        if (diagnosis == null) {
            return Result.error("诊断记录不存在");
        }
        return Result.ok(diagnosis);
    }

    @GetMapping("/list")
    public Result<List<AgentDiagnosis>> listByVin(@RequestParam String vin) {
        List<AgentDiagnosis> list = agentDiagnosisService.getByVin(vin);
        return Result.ok(list);
    }

    @GetMapping("/by-fault/{faultId}")
    public Result<List<AgentDiagnosis>> listByFaultId(@PathVariable Long faultId) {
        List<AgentDiagnosis> list = agentDiagnosisService.getByFaultId(faultId);
        return Result.ok(list);
    }
}
