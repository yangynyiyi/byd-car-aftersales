package com.byd.aftersales.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AgentDiagnosis {

    private Long diagnosisId;
    private Long faultId;
    private String inputText;
    private String diagnosisSuggestion;
    private String riskLevel;
    private String recommendedChecks;
    private BigDecimal confidenceScore;
    private String agentName;
    private String rawResponse;
    private LocalDateTime createdAt;

    public Long getDiagnosisId() { return diagnosisId; }
    public void setDiagnosisId(Long diagnosisId) { this.diagnosisId = diagnosisId; }
    public Long getFaultId() { return faultId; }
    public void setFaultId(Long faultId) { this.faultId = faultId; }
    public String getInputText() { return inputText; }
    public void setInputText(String inputText) { this.inputText = inputText; }
    public String getDiagnosisSuggestion() { return diagnosisSuggestion; }
    public void setDiagnosisSuggestion(String diagnosisSuggestion) { this.diagnosisSuggestion = diagnosisSuggestion; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRecommendedChecks() { return recommendedChecks; }
    public void setRecommendedChecks(String recommendedChecks) { this.recommendedChecks = recommendedChecks; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }
    public String getRawResponse() { return rawResponse; }
    public void setRawResponse(String rawResponse) { this.rawResponse = rawResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
