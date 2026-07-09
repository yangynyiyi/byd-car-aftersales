package com.byd.aftersales.dao;

import com.byd.aftersales.domain.AgentDiagnosis;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class AgentDiagnosisDao extends BaseJdbcDao {

    private final RowMapper<AgentDiagnosis> rowMapper = (rs, rowNum) -> {
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
        Timestamp createdAt = rs.getTimestamp("created_at");
        d.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return d;
    };

    public AgentDiagnosisDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(AgentDiagnosis diagnosis) {
        String sql = """
                INSERT INTO agent_diagnosis
                    (fault_id, input_text, diagnosis_suggestion, risk_level,
                     recommended_checks, confidence_score, agent_name, raw_response)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, diagnosis.getFaultId());
            ps.setString(2, diagnosis.getInputText());
            ps.setString(3, diagnosis.getDiagnosisSuggestion());
            ps.setString(4, diagnosis.getRiskLevel());
            ps.setString(5, diagnosis.getRecommendedChecks());
            ps.setBigDecimal(6, diagnosis.getConfidenceScore());
            ps.setString(7, diagnosis.getAgentName());
            ps.setString(8, diagnosis.getRawResponse());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<AgentDiagnosis> findById(Long diagnosisId) {
        List<AgentDiagnosis> list = jdbc().query(
                "SELECT * FROM agent_diagnosis WHERE diagnosis_id = ?", rowMapper, diagnosisId);
        return list.stream().findFirst();
    }

    public List<AgentDiagnosis> findByFaultId(Long faultId) {
        return jdbc().query(
                "SELECT * FROM agent_diagnosis WHERE fault_id = ? ORDER BY created_at DESC",
                rowMapper, faultId);
    }
}
