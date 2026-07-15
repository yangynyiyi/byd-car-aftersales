package com.byd.aftersales.dao;

import com.byd.aftersales.domain.WorkOrder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class WorkOrderDao extends BaseJdbcDao {

    private final RowMapper<WorkOrder> rowMapper = (rs, rowNum) -> {
        WorkOrder wo = new WorkOrder();
        wo.setWorkOrderId(rs.getLong("work_order_id"));
        wo.setWorkOrderNo(rs.getString("work_order_no"));
        wo.setFaultId(rs.getLong("fault_id"));
        long diagId = rs.getLong("diagnosis_id");
        wo.setDiagnosisId(rs.wasNull() ? null : diagId);
        long techId = rs.getLong("technician_id");
        wo.setTechnicianId(rs.wasNull() ? null : techId);
        Timestamp assignedAt = rs.getTimestamp("assigned_at");
        wo.setAssignedAt(assignedAt == null ? null : assignedAt.toLocalDateTime());
        wo.setStatus(rs.getString("status"));
        wo.setLaborCost(rs.getBigDecimal("labor_cost"));
        wo.setRepairResult(rs.getString("repair_result"));
        Timestamp startedAt = rs.getTimestamp("started_at");
        wo.setStartedAt(startedAt == null ? null : startedAt.toLocalDateTime());
        Timestamp partWaitingAt = rs.getTimestamp("part_waiting_at");
        wo.setPartWaitingAt(partWaitingAt == null ? null : partWaitingAt.toLocalDateTime());
        Timestamp partsArrivedAt = rs.getTimestamp("parts_arrived_at");
        wo.setPartsArrivedAt(partsArrivedAt == null ? null : partsArrivedAt.toLocalDateTime());
        Timestamp finishedAt = rs.getTimestamp("finished_at");
        wo.setFinishedAt(finishedAt == null ? null : finishedAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        wo.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        wo.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        wo.setDeleted(rs.getInt("deleted"));
        return wo;
    };

    public WorkOrderDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(WorkOrder wo) {
        String sql = """
                INSERT INTO work_order
                    (work_order_no, fault_id, diagnosis_id, technician_id, assigned_at, status, labor_cost)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, wo.getWorkOrderNo());
            ps.setLong(2, wo.getFaultId());
            if (wo.getDiagnosisId() == null) {
                ps.setObject(3, null);
            } else {
                ps.setLong(3, wo.getDiagnosisId());
            }
            if (wo.getTechnicianId() == null) {
                ps.setObject(4, null);
                ps.setObject(5, null);
            } else {
                ps.setLong(4, wo.getTechnicianId());
                ps.setTimestamp(5, Timestamp.valueOf(
                        wo.getAssignedAt() != null ? wo.getAssignedAt() : java.time.LocalDateTime.now()));
            }
            ps.setString(6, wo.getStatus());
            ps.setBigDecimal(7, wo.getLaborCost());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<WorkOrder> findById(Long id) {
        List<WorkOrder> list = jdbc().query(
                "SELECT * FROM work_order WHERE work_order_id = ? AND deleted = 0", rowMapper, id);
        return list.stream().findFirst();
    }

    public List<WorkOrder> findByTechnicianId(Long technicianId) {
        return jdbc().query(
                "SELECT * FROM work_order WHERE technician_id = ? AND deleted = 0 ORDER BY created_at DESC",
                rowMapper, technicianId);
    }

    public List<WorkOrder> findAll() {
        return jdbc().query("SELECT * FROM work_order WHERE deleted = 0 ORDER BY created_at DESC", rowMapper);
    }

    public boolean existsByFaultId(Long faultId) {
        Long count = jdbc().queryForObject(
                "SELECT COUNT(*) FROM work_order WHERE fault_id = ? AND deleted = 0", Long.class, faultId);
        return count != null && count > 0;
    }

    public int assignTechnician(Long workOrderId, Long technicianId, BigDecimal laborCost) {
        return jdbc().update("""
                UPDATE work_order
                SET technician_id = ?, assigned_at = NOW(), status = 'ASSIGNED',
                    labor_cost = ?, updated_at = NOW()
                WHERE work_order_id = ? AND deleted = 0
                  AND status NOT IN ('COMPLETED', 'CANCELLED')
                """, technicianId, laborCost, workOrderId);
    }

    public int updateLaborCost(Long workOrderId, BigDecimal laborCost) {
        return jdbc().update("""
                UPDATE work_order SET labor_cost = ?, updated_at = NOW()
                WHERE work_order_id = ? AND deleted = 0
                  AND status NOT IN ('COMPLETED', 'CANCELLED')
                """, laborCost, workOrderId);
    }

    public int markStarted(Long workOrderId) {
        return jdbc().update("""
                UPDATE work_order
                SET status = 'IN_PROGRESS', started_at = NOW(), updated_at = NOW()
                WHERE work_order_id = ? AND deleted = 0 AND status = 'ASSIGNED'
                """, workOrderId);
    }

    public int partsArrived(Long workOrderId) {
        return jdbc().update("""
                UPDATE work_order
                SET status = 'IN_PROGRESS', parts_arrived_at = NOW(), updated_at = NOW()
                WHERE work_order_id = ? AND deleted = 0 AND status = 'PART_WAITING'
                """, workOrderId);
    }

    public int markPartWaiting(Long workOrderId) {
        return jdbc().update("""
                UPDATE work_order
                SET status = 'PART_WAITING', part_waiting_at = NOW(), updated_at = NOW()
                WHERE work_order_id = ? AND deleted = 0 AND status = 'IN_PROGRESS'
                """, workOrderId);
    }

    public int complete(Long workOrderId, String repairResult) {
        return jdbc().update("""
                UPDATE work_order
                SET status = 'COMPLETED', repair_result = ?, finished_at = NOW(), updated_at = NOW()
                WHERE work_order_id = ? AND deleted = 0
                  AND status IN ('IN_PROGRESS', 'PART_WAITING')
                """, repairResult, workOrderId);
    }

    public long countByStatus(String status) {
        Long count = jdbc().queryForObject(
                "SELECT COUNT(*) FROM work_order WHERE status = ? AND deleted = 0", Long.class, status);
        return count == null ? 0 : count;
    }

    public List<WorkOrder> findRecentByVin(String vin, int limit) {
        return jdbc().query("""
                SELECT wo.* FROM work_order wo
                JOIN fault_record f ON wo.fault_id = f.fault_id
                WHERE f.vin = ? AND wo.deleted = 0
                ORDER BY wo.created_at DESC
                LIMIT ?
                """, rowMapper, vin, limit);
    }

    public long countActiveByVin(String vin) {
        Long count = jdbc().queryForObject("""
                SELECT COUNT(*)
                FROM work_order wo
                JOIN fault_record f ON wo.fault_id = f.fault_id AND f.deleted = 0
                WHERE f.vin = ? AND wo.deleted = 0
                  AND wo.status NOT IN ('COMPLETED', 'CANCELLED')
                """, Long.class, vin);
        return count == null ? 0 : count;
    }
}
