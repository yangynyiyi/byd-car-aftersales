package com.byd.aftersales.controller;

import com.byd.aftersales.common.ApiResponse;
import com.byd.aftersales.domain.AgentDiagnosis;
import com.byd.aftersales.dto.AgentDiagnoseRequest;
import com.byd.aftersales.service.AgentAssistantService;
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
    private final AgentAssistantService agentAssistantService;

    public AgentDiagnosisController(AgentDiagnosisService agentDiagnosisService,
                                    AgentAssistantService agentAssistantService) {
        this.agentDiagnosisService = agentDiagnosisService;
        this.agentAssistantService = agentAssistantService;
    }

    @PostMapping("/diagnose")
    public ApiResponse<AgentDiagnosis> diagnose(@RequestBody AgentDiagnoseRequest request) {
        return ApiResponse.ok(agentDiagnosisService.diagnose(request));
    }

    @GetMapping("/fault/{faultId}")
    public ApiResponse<List<AgentDiagnosis>> listByFault(@PathVariable("faultId") Long faultId) {
        return ApiResponse.ok(agentDiagnosisService.listByFaultId(faultId));
    }

    @GetMapping("/vin/{vin}")
    public ApiResponse<List<AgentDiagnosis>> listByVin(@PathVariable("vin") String vin) {
        return ApiResponse.ok(agentDiagnosisService.listByVin(vin));
    }

    @GetMapping("/{id}")
    public ApiResponse<AgentDiagnosis> getById(@PathVariable("id") Long id) {
        return ApiResponse.ok(agentDiagnosisService.getById(id));
    }

    @PostMapping("/assistant/script")
    public ApiResponse<String> generateScript(@RequestBody AgentDiagnoseRequest request) {
        return ApiResponse.ok(agentAssistantService.generateCustomerScript(request.getDiagnosisId()));
    }

    @PostMapping("/assistant/quote")
    public ApiResponse<String> generateQuote(@RequestBody AgentDiagnoseRequest request) {
        return ApiResponse.ok(agentAssistantService.generateRepairQuote(request.getDiagnosisId()));
    }

    @PostMapping("/assistant/maintenance")
    public ApiResponse<String> generateMaintenance(@RequestBody AgentDiagnoseRequest request) {
        return ApiResponse.ok(agentAssistantService.generateMaintenancePlan(request.getVin()));
    }
}
