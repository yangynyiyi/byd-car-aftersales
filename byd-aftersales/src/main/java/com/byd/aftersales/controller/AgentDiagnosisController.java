package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.AgentDiagnosis;
import com.byd.aftersales.dto.AgentDiagnoseRequest;
import com.byd.aftersales.service.AgentDiagnosisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class AgentDiagnosisController {

    private final AgentDiagnosisService agentDiagnosisService;

    public AgentDiagnosisController(AgentDiagnosisService agentDiagnosisService) {
        this.agentDiagnosisService = agentDiagnosisService;
    }

    @PostMapping("/diagnose")
    public ApiResponse<AgentDiagnosis> diagnose(@RequestBody AgentDiagnoseRequest request) {
        return ApiResponse.ok(agentDiagnosisService.diagnose(request));
    }

    @GetMapping("/fault/{faultId}")
    public ApiResponse<List<AgentDiagnosis>> listByFault(@PathVariable("faultId") Long faultId) {
        return ApiResponse.ok(agentDiagnosisService.listByFaultId(faultId));
    }
}
