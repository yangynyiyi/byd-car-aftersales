package com.byd.car.part.dao;

import com.byd.car.part.entity.PartUsage;
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
public class PartUsageDao extends JdbcDaoSupport {

    public PartUsageDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Long insert(PartUsage usage) {
        String sql = """
                INSERT INTO part_usage
                    (work_order_id, part_id, quantity, unit_price, technician_id, status)
                VALUES (?, ?, ?, ?, ?, 'APPLIED')
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(con -> {
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

    public PartUsage findById(Long usageId) {
        String sql = "SELECT * FROM part_usage WHERE usage_id = ?";
        List<PartUsage> list = getJdbcTemplate().query(sql, new PartUsageRowMapper(), usageId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<PartUsage> findByWorkOrderId(Long workOrderId) {
        String sql = "SELECT * FROM part_usage WHERE work_order_id = ? ORDER BY created_at DESC";
        return getJdbcTemplate().query(sql, new PartUsageRowMapper(), workOrderId);
    }

    /** 查询工单下所有已审批（APPROVED）的备件申请 */
    public List<PartUsage> findApprovedByWorkOrderId(Long workOrderId) {
        String sql = "SELECT * FROM part_usage WHERE work_order_id = ? AND status = 'APPROVED'";
        return getJdbcTemplate().query(sql, new PartUsageRowMapper(), workOrderId);
    }

    public int approve(Long usageId, Long approvedBy) {
        String sql = """
                UPDATE part_usage
                SET status = 'APPROVED', approved_by = ?, approved_at = NOW()
                WHERE usage_id = ? AND status = 'APPLIED'
                """;
        return getJdbcTemplate().update(sql, approvedBy, usageId);
    }

    public int reject(Long usageId, Long approvedBy) {
        String sql = """
                UPDATE part_usage
                SET status = 'REJECTED', approved_by = ?, approved_at = NOW()
                WHERE usage_id = ? AND status = 'APPLIED'
                """;
        return getJdbcTemplate().update(sql, approvedBy, usageId);
    }

    public int markUsed(Long usageId) {
        String sql = "UPDATE part_usage SET status = 'USED' WHERE usage_id = ? AND status = 'APPROVED'";
        return getJdbcTemplate().update(sql, usageId);
    }

    private static class PartUsageRowMapper implements RowMapper<PartUsage> {
        @Override
        public PartUsage mapRow(ResultSet rs, int rowNum) throws SQLException {
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
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            Timestamp approvedAt = rs.getTimestamp("approved_at");
            u.setApprovedAt(approvedAt != null ? approvedAt.toLocalDateTime() : null);
            return u;
        }
    }
}
