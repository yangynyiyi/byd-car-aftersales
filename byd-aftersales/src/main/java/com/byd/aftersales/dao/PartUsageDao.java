package com.byd.aftersales.dao;

import com.byd.aftersales.domain.PartUsage;
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
public class PartUsageDao extends BaseJdbcDao {

    private final RowMapper<PartUsage> rowMapper = (rs, rowNum) -> {
        PartUsage u = new PartUsage();
        u.setUsageId(rs.getLong("usage_id"));
        u.setWorkOrderId(rs.getLong("work_order_id"));
        u.setPartId(rs.getLong("part_id"));
        u.setQuantity(rs.getInt("quantity"));
        u.setUnitPrice(rs.getBigDecimal("unit_price"));
        u.setTechnicianId(rs.getLong("technician_id"));
        long approvedBy = rs.getLong("approved_by");
        u.setApprovedBy(rs.wasNull() ? null : approvedBy);
        u.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        u.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp approvedAt = rs.getTimestamp("approved_at");
        u.setApprovedAt(approvedAt == null ? null : approvedAt.toLocalDateTime());
        return u;
    };

    public PartUsageDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(PartUsage usage) {
        String sql = """
                INSERT INTO part_usage
                    (work_order_id, part_id, quantity, unit_price, technician_id, status)
                VALUES (?, ?, ?, ?, ?, 'APPLIED')
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, usage.getWorkOrderId());
            ps.setLong(2, usage.getPartId());
            ps.setInt(3, usage.getQuantity());
            ps.setBigDecimal(4, usage.getUnitPrice());
            ps.setLong(5, usage.getTechnicianId());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<PartUsage> findById(Long usageId) {
        List<PartUsage> list = jdbc().query("SELECT * FROM part_usage WHERE usage_id = ?", rowMapper, usageId);
        return list.stream().findFirst();
    }

    public List<PartUsage> findByWorkOrderId(Long workOrderId) {
        return jdbc().query(
                "SELECT * FROM part_usage WHERE work_order_id = ? ORDER BY created_at DESC",
                rowMapper, workOrderId);
    }

    public List<PartUsage> findApprovedByWorkOrderId(Long workOrderId) {
        return jdbc().query(
                "SELECT * FROM part_usage WHERE work_order_id = ? AND status = 'APPROVED'",
                rowMapper, workOrderId);
    }

    public List<PartUsage> findByStatus(String status) {
        return jdbc().query(
                "SELECT * FROM part_usage WHERE status = ? ORDER BY created_at DESC",
                rowMapper, status);
    }

    /** 待备件员审批（含历史 PROPOSED 遗留数据） */
    public List<PartUsage> findPendingApproval() {
        return jdbc().query(
                "SELECT * FROM part_usage WHERE status IN ('APPLIED', 'PROPOSED') ORDER BY created_at DESC",
                rowMapper);
    }

    public List<PartUsage> findAll() {
        return jdbc().query("SELECT * FROM part_usage ORDER BY created_at DESC", rowMapper);
    }

    public long countByDate(java.time.LocalDate date) {
        Long count = jdbc().queryForObject(
                "SELECT COUNT(*) FROM part_usage WHERE DATE(created_at) = ?",
                Long.class, date.toString());
        return count == null ? 0 : count;
    }

    public boolean existsPendingByWorkOrderAndPart(Long workOrderId, Long partId) {
        Integer count = jdbc().queryForObject("""
                SELECT COUNT(*) FROM part_usage
                WHERE work_order_id = ? AND part_id = ?
                  AND status IN ('PROPOSED', 'APPLIED', 'APPROVED')
                """, Integer.class, workOrderId, partId);
        return count != null && count > 0;
    }

    public int approve(Long usageId, Long approvedBy) {
        return jdbc().update("""
                UPDATE part_usage
                SET status = 'APPROVED', approved_by = ?, approved_at = NOW()
                WHERE usage_id = ? AND status IN ('APPLIED', 'PROPOSED')
                """, approvedBy, usageId);
    }

    public int reject(Long usageId, Long approvedBy) {
        return jdbc().update("""
                UPDATE part_usage
                SET status = 'REJECTED', approved_by = ?, approved_at = NOW()
                WHERE usage_id = ? AND status IN ('APPLIED', 'PROPOSED')
                """, approvedBy, usageId);
    }

    public int markUsed(Long usageId) {
        return jdbc().update(
                "UPDATE part_usage SET status = 'USED' WHERE usage_id = ? AND status = 'APPROVED'", usageId);
    }
}
