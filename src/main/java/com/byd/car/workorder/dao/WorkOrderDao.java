package com.byd.car.workorder.dao;

import com.byd.car.workorder.entity.WorkOrder;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
public class WorkOrderDao extends JdbcDaoSupport {

    public WorkOrderDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Long insert(WorkOrder wo) {
        String sql = """
                INSERT INTO work_order
                    (work_order_no, fault_id, diagnosis_id, technician_id, status, labor_cost)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, wo.getWorkOrderNo());
            ps.setLong(2, wo.getFaultId());
            ps.setObject(3, wo.getDiagnosisId());
            ps.setObject(4, wo.getTechnicianId());
            ps.setString(5, wo.getStatus());
            ps.setBigDecimal(6, wo.getLaborCost());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public WorkOrder findById(Long id) {
        String sql = "SELECT * FROM work_order WHERE work_order_id = ? AND deleted = 0";
        List<WorkOrder> list = getJdbcTemplate().query(sql, new WorkOrderRowMapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public WorkOrder findByWorkOrderNo(String workOrderNo) {
        String sql = "SELECT * FROM work_order WHERE work_order_no = ? AND deleted = 0";
        List<WorkOrder> list = getJdbcTemplate().query(sql, new WorkOrderRowMapper(), workOrderNo);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<WorkOrder> findByTechnicianId(Long technicianId) {
        String sql = "SELECT * FROM work_order WHERE technician_id = ? AND deleted = 0 ORDER BY created_at DESC";
        return getJdbcTemplate().query(sql, new WorkOrderRowMapper(), technicianId);
    }

    public List<WorkOrder> findAll() {
        String sql = "SELECT * FROM work_order WHERE deleted = 0 ORDER BY created_at DESC";
        return getJdbcTemplate().query(sql, new WorkOrderRowMapper());
    }

    public int updateStatus(Long workOrderId, String status) {
        String sql = "UPDATE work_order SET status = ? WHERE work_order_id = ?";
        return getJdbcTemplate().update(sql, status, workOrderId);
    }

    public int assignTechnician(Long workOrderId, Long technicianId) {
        String sql = "UPDATE work_order SET technician_id = ?, status = 'ASSIGNED' WHERE work_order_id = ?";
        return getJdbcTemplate().update(sql, technicianId, workOrderId);
    }

    public int complete(Long workOrderId, String repairResult) {
        String sql = """
                UPDATE work_order
                SET status = 'COMPLETED', repair_result = ?, finished_at = NOW()
                WHERE work_order_id = ?
                """;
        return getJdbcTemplate().update(sql, repairResult, workOrderId);
    }

    public int markStarted(Long workOrderId) {
        String sql = "UPDATE work_order SET status = 'IN_PROGRESS', started_at = NOW() WHERE work_order_id = ?";
        return getJdbcTemplate().update(sql, workOrderId);
    }

    private static class WorkOrderRowMapper implements RowMapper<WorkOrder> {
        @Override
        public WorkOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
            WorkOrder wo = new WorkOrder();
            wo.setWorkOrderId(rs.getLong("work_order_id"));
            wo.setWorkOrderNo(rs.getString("work_order_no"));
            wo.setFaultId(rs.getLong("fault_id"));
            long diagId = rs.getLong("diagnosis_id");
            wo.setDiagnosisId(rs.wasNull() ? null : diagId);
            long techId = rs.getLong("technician_id");
            wo.setTechnicianId(rs.wasNull() ? null : techId);
            wo.setStatus(rs.getString("status"));
            wo.setLaborCost(rs.getBigDecimal("labor_cost"));
            wo.setRepairResult(rs.getString("repair_result"));
            Timestamp startedAt = rs.getTimestamp("started_at");
            wo.setStartedAt(startedAt != null ? startedAt.toLocalDateTime() : null);
            Timestamp finishedAt = rs.getTimestamp("finished_at");
            wo.setFinishedAt(finishedAt != null ? finishedAt.toLocalDateTime() : null);
            wo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            wo.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            wo.setDeleted(rs.getInt("deleted"));
            return wo;
        }
    }
}
