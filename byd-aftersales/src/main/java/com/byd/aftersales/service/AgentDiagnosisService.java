package com.byd.aftersales.service;

import com.byd.aftersales.domain.AgentDiagnosis;

import java.util.List;

public interface AgentDiagnosisService {

    AgentDiagnosis diagnose(com.byd.aftersales.dto.AgentDiagnoseRequest request);

    AgentDiagnosis getById(Long diagnosisId);

    List<AgentDiagnosis> listByFaultId(Long faultId);

    List<AgentDiagnosis> listByVin(String vin);
}
