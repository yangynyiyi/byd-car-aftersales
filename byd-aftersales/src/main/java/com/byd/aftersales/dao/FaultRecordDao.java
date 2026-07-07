package com.byd.aftersales.dao;

import com.byd.aftersales.domain.FaultRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class FaultRecordDao extends BaseJdbcDao {

    private final RowMapper<FaultRecord> rowMapper = (rs, rowNum) -> {
        FaultRecord record = new FaultRecord();
        record.setFaultId(rs.getLong("fault_id"));
        record.setFaultNo(rs.getString("fault_no"));
        long appointmentId = rs.getLong("appointment_id");
        record.setAppointmentId(rs.wasNull() ? null : appointmentId);
        record.setVin(rs.getString("vin"));
        record.setOwnerId(rs.getLong("owner_id"));
        record.setAdvisorId(rs.getLong("advisor_id"));
        record.setFaultDescription(rs.getString("fault_description"));
        record.setFaultLevel(rs.getString("fault_level"));
        record.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        record.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        record.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        record.setDeleted(rs.getInt("deleted"));
        return record;
    };

    public FaultRecordDao(DataSource dataSource) {
        super(dataSource);
    }

    public int insert(FaultRecord record) {
        String sql = """
                INSERT INTO fault_record
                (fault_no, appointment_id, vin, owner_id, advisor_id, fault_description, fault_level, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbc().update(sql, record.getFaultNo(), record.getAppointmentId(), record.getVin(),
                record.getOwnerId(), record.getAdvisorId(), record.getFaultDescription(),
                record.getFaultLevel(), record.getStatus());
    }

    public int updateStatus(String faultNo, String status) {
        return jdbc().update("UPDATE fault_record SET status = ? WHERE fault_no = ? AND deleted = 0",
                status, faultNo);
    }

    public int softDelete(String faultNo) {
        return jdbc().update("UPDATE fault_record SET deleted = 1 WHERE fault_no = ?", faultNo);
    }

    public Optional<FaultRecord> findByNo(String faultNo) {
        List<FaultRecord> records = jdbc().query("SELECT * FROM fault_record WHERE fault_no = ? AND deleted = 0",
                rowMapper, faultNo);
        return records.stream().findFirst();
    }

    public List<FaultRecord> findByVin(String vin) {
        return jdbc().query("""
                SELECT * FROM fault_record WHERE vin = ? AND deleted = 0 ORDER BY created_at DESC
                """, rowMapper, vin);
    }

    public List<FaultRecord> findAll() {
        return jdbc().query("SELECT * FROM fault_record WHERE deleted = 0 ORDER BY created_at DESC", rowMapper);
    }
}
