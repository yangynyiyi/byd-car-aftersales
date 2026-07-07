package com.byd.car.settlement.dao;

import com.byd.car.settlement.entity.Settlement;
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
public class SettlementDao extends JdbcDaoSupport {

    public SettlementDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Long insert(Settlement s) {
        String sql = """
                INSERT INTO settlement
                    (settlement_no, work_order_id, labor_amount, part_amount,
                     warranty_amount, total_amount, payment_status)
                VALUES (?, ?, ?, ?, ?, ?, 'UNPAID')
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getSettlementNo());
            ps.setLong(2, s.getWorkOrderId());
            ps.setBigDecimal(3, s.getLaborAmount());
            ps.setBigDecimal(4, s.getPartAmount());
            ps.setBigDecimal(5, s.getWarrantyAmount());
            ps.setBigDecimal(6, s.getTotalAmount());
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Settlement findById(Long settlementId) {
        String sql = "SELECT * FROM settlement WHERE settlement_id = ?";
        List<Settlement> list = getJdbcTemplate().query(sql, new SettlementRowMapper(), settlementId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Settlement findByWorkOrderId(Long workOrderId) {
        String sql = "SELECT * FROM settlement WHERE work_order_id = ?";
        List<Settlement> list = getJdbcTemplate().query(sql, new SettlementRowMapper(), workOrderId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int markPaid(Long settlementId) {
        String sql = """
                UPDATE settlement
                SET payment_status = 'PAID', paid_at = NOW()
                WHERE settlement_id = ? AND payment_status = 'UNPAID'
                """;
        return getJdbcTemplate().update(sql, settlementId);
    }

    private static class SettlementRowMapper implements RowMapper<Settlement> {
        @Override
        public Settlement mapRow(ResultSet rs, int rowNum) throws SQLException {
            Settlement s = new Settlement();
            s.setSettlementId(rs.getLong("settlement_id"));
            s.setSettlementNo(rs.getString("settlement_no"));
            s.setWorkOrderId(rs.getLong("work_order_id"));
            s.setLaborAmount(rs.getBigDecimal("labor_amount"));
            s.setPartAmount(rs.getBigDecimal("part_amount"));
            s.setWarrantyAmount(rs.getBigDecimal("warranty_amount"));
            s.setTotalAmount(rs.getBigDecimal("total_amount"));
            s.setPaymentStatus(rs.getString("payment_status"));
            Timestamp paidAt = rs.getTimestamp("paid_at");
            s.setPaidAt(paidAt != null ? paidAt.toLocalDateTime() : null);
            s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return s;
        }
    }
}
