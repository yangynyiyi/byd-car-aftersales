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

    private static final String BASE_SELECT = """
            SELECT f.*, own.real_name AS owner_name, adv.real_name AS advisor_name,
                   veh.license_plate, veh.model
            FROM fault_record f
            LEFT JOIN sys_user own ON f.owner_id = own.user_id
            LEFT JOIN sys_user adv ON f.advisor_id = adv.user_id
            LEFT JOIN vehicle veh ON f.vin = veh.vin AND veh.deleted = 0
            """;

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
        record.setOwnerName(rs.getString("owner_name"));
        record.setAdvisorName(rs.getString("advisor_name"));
        record.setLicensePlate(rs.getString("license_plate"));
        record.setModel(rs.getString("model"));
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

    public int update(FaultRecord record) {
        String sql = """
                UPDATE fault_record
                SET vin = ?, owner_id = ?, advisor_id = ?, fault_description = ?,
                    fault_level = ?, status = ?
                WHERE fault_no = ? AND deleted = 0
                """;
        return jdbc().update(sql, record.getVin(), record.getOwnerId(), record.getAdvisorId(),
                record.getFaultDescription(), record.getFaultLevel(), record.getStatus(), record.getFaultNo());
    }

    public int softDelete(String faultNo) {
        return jdbc().update("UPDATE fault_record SET deleted = 1 WHERE fault_no = ?", faultNo);
    }

    public Optional<FaultRecord> findById(Long faultId) {
        List<FaultRecord> records = jdbc().query(
                BASE_SELECT + " WHERE f.fault_id = ? AND f.deleted = 0",
                rowMapper, faultId);
        return records.stream().findFirst();
    }

    public Optional<FaultRecord> findByNo(String faultNo) {
        List<FaultRecord> records = jdbc().query(
                BASE_SELECT + " WHERE f.fault_no = ? AND f.deleted = 0",
                rowMapper, faultNo);
        return records.stream().findFirst();
    }

    public List<FaultRecord> findByVin(String vin) {
        return jdbc().query(
                BASE_SELECT + " WHERE f.vin = ? AND f.deleted = 0 ORDER BY f.created_at DESC",
                rowMapper, vin);
    }

    public Optional<FaultRecord> findByAppointmentId(Long appointmentId) {
        List<FaultRecord> records = jdbc().query(
                BASE_SELECT + " WHERE f.appointment_id = ? AND f.deleted = 0 LIMIT 1",
                rowMapper, appointmentId);
        return records.stream().findFirst();
    }

    public List<FaultRecord> findAll() {
        return jdbc().query(
                BASE_SELECT + " WHERE f.deleted = 0 ORDER BY f.created_at DESC",
                rowMapper);
    }
}
