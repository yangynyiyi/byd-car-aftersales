package com.byd.agent.dao;

import com.byd.agent.model.AgentDiagnosis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class AgentDiagnosisDao extends JdbcDaoSupport {

    @Autowired
    public void setDataSourceInternal(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public int insert(AgentDiagnosis record) {
        String sql = "INSERT INTO agent_diagnosis " +
                "(fault_id, input_text, diagnosis_suggestion, risk_level, " +
                "recommended_checks, confidence_score, agent_name, raw_response) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return getJdbcTemplate().update(sql,
                record.getFaultId(),
                record.getInputText(),
                record.getDiagnosisSuggestion(),
                record.getRiskLevel(),
                record.getRecommendedChecks(),
                record.getConfidenceScore(),
                record.getAgentName(),
                record.getRawResponse());
    }

    public AgentDiagnosis selectById(Long diagnosisId) {
        String sql = "SELECT * FROM agent_diagnosis WHERE diagnosis_id = ?";
        List<AgentDiagnosis> list = getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    AgentDiagnosis d = new AgentDiagnosis();
                    d.setDiagnosisId(rs.getLong("diagnosis_id"));
                    d.setFaultId(rs.getLong("fault_id"));
                    d.setInputText(rs.getString("input_text"));
                    d.setDiagnosisSuggestion(rs.getString("diagnosis_suggestion"));
                    d.setRiskLevel(rs.getString("risk_level"));
                    d.setRecommendedChecks(rs.getString("recommended_checks"));
                    d.setConfidenceScore(rs.getBigDecimal("confidence_score"));
                    d.setAgentName(rs.getString("agent_name"));
                    d.setRawResponse(rs.getString("raw_response"));
                    d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return d;
                },
                diagnosisId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<AgentDiagnosis> selectByFaultId(Long faultId) {
        String sql = "SELECT * FROM agent_diagnosis WHERE fault_id = ? ORDER BY created_at DESC";
        return getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    AgentDiagnosis d = new AgentDiagnosis();
                    d.setDiagnosisId(rs.getLong("diagnosis_id"));
                    d.setFaultId(rs.getLong("fault_id"));
                    d.setInputText(rs.getString("input_text"));
                    d.setDiagnosisSuggestion(rs.getString("diagnosis_suggestion"));
                    d.setRiskLevel(rs.getString("risk_level"));
                    d.setRecommendedChecks(rs.getString("recommended_checks"));
                    d.setConfidenceScore(rs.getBigDecimal("confidence_score"));
                    d.setAgentName(rs.getString("agent_name"));
                    d.setRawResponse(rs.getString("raw_response"));
                    d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return d;
                },
                faultId);
    }

    public List<AgentDiagnosis> selectByVin(String vin) {
        String sql = "SELECT ad.* FROM agent_diagnosis ad " +
                "JOIN fault_record f ON ad.fault_id = f.fault_id " +
                "WHERE f.vin = ? ORDER BY ad.created_at DESC";
        return getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    AgentDiagnosis d = new AgentDiagnosis();
                    d.setDiagnosisId(rs.getLong("diagnosis_id"));
                    d.setFaultId(rs.getLong("fault_id"));
                    d.setInputText(rs.getString("input_text"));
                    d.setDiagnosisSuggestion(rs.getString("diagnosis_suggestion"));
                    d.setRiskLevel(rs.getString("risk_level"));
                    d.setRecommendedChecks(rs.getString("recommended_checks"));
                    d.setConfidenceScore(rs.getBigDecimal("confidence_score"));
                    d.setAgentName(rs.getString("agent_name"));
                    d.setRawResponse(rs.getString("raw_response"));
                    d.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return d;
                },
                vin);
    }
}
