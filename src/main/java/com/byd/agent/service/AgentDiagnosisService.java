package com.byd.agent.service;

import com.byd.agent.model.AgentDiagnosis;
import com.byd.agent.model.AgentDiagnosisResult;

import java.util.List;

public interface AgentDiagnosisService {

    AgentDiagnosisResult diagnose(Long faultId);

    AgentDiagnosis getById(Long diagnosisId);

    List<AgentDiagnosis> getByFaultId(Long faultId);

    List<AgentDiagnosis> getByVin(String vin);
}
