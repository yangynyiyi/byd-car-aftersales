package com.byd.agent.model;

import java.math.BigDecimal;

public class AgentDiagnosisResult {

    private String diagnosisSuggestion;
    private String riskLevel;
    private String recommendedChecks;
    private BigDecimal confidenceScore;

    public String getDiagnosisSuggestion() { return diagnosisSuggestion; }
    public void setDiagnosisSuggestion(String diagnosisSuggestion) { this.diagnosisSuggestion = diagnosisSuggestion; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getRecommendedChecks() { return recommendedChecks; }
    public void setRecommendedChecks(String recommendedChecks) { this.recommendedChecks = recommendedChecks; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(BigDecimal confidenceScore) { this.confidenceScore = confidenceScore; }
}
