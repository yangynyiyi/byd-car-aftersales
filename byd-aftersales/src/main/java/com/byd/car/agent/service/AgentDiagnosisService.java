package com.byd.car.agent.service;

import com.byd.car.agent.model.AgentDiagnosis;
import com.byd.car.agent.model.AgentDiagnosisResult;

import java.util.List;

public interface AgentDiagnosisService {

    AgentDiagnosisResult diagnose(Long faultId);

    AgentDiagnosis getById(Long diagnosisId);

    List<AgentDiagnosis> getByFaultId(Long faultId);

    List<AgentDiagnosis> getByVin(String vin);
}
