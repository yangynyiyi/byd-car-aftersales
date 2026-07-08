package com.byd.aftersales.dao;

import com.byd.aftersales.domain.Settlement;
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
public class SettlementDao extends BaseJdbcDao {

    private final RowMapper<Settlement> rowMapper = (rs, rowNum) -> {
        Settlement s = new Settlement();
        s.setSettlementId(rs.getLong("settlement_id"));
        s.setSettlementNo(rs.getString("settlement_no"));
        s.setWorkOrderId(rs.getLong("work_order_id"));
        s.setLaborAmount(rs.getBigDecimal("labor_amount"));
        s.setPartAmount(rs.getBigDecimal("part_amount"));
        s.setWarrantyAmount(rs.getBigDecimal("warranty_amount"));
        s.setTotalAmount(rs.getBigDecimal("total_amount"));
        s.setPaymentStatus(rs.getString("payment_status"));
        s.setManagerStatus(rs.getString("manager_status"));
        long approvedBy = rs.getLong("approved_by");
        s.setApprovedBy(rs.wasNull() ? null : approvedBy);
        Timestamp approvedAt = rs.getTimestamp("approved_at");
        s.setApprovedAt(approvedAt == null ? null : approvedAt.toLocalDateTime());
        Timestamp paidAt = rs.getTimestamp("paid_at");
        s.setPaidAt(paidAt == null ? null : paidAt.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        s.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        return s;
    };

    public SettlementDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(Settlement s) {
        String sql = """
                INSERT INTO settlement
                    (settlement_no, work_order_id, labor_amount, part_amount,
                     warranty_amount, total_amount, payment_status, manager_status)
                VALUES (?, ?, ?, ?, ?, ?, 'UNPAID', 'PENDING_APPROVAL')
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
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

    public Optional<Settlement> findById(Long settlementId) {
        List<Settlement> list = jdbc().query("SELECT * FROM settlement WHERE settlement_id = ?", rowMapper, settlementId);
        return list.stream().findFirst();
    }

    public Optional<Settlement> findByWorkOrderId(Long workOrderId) {
        List<Settlement> list = jdbc().query(
                "SELECT * FROM settlement WHERE work_order_id = ?", rowMapper, workOrderId);
        return list.stream().findFirst();
    }

    public List<Settlement> findAll() {
        return jdbc().query("SELECT * FROM settlement ORDER BY created_at DESC", rowMapper);
    }

    public int markPaid(Long settlementId) {
        return jdbc().update("""
                UPDATE settlement
                SET payment_status = 'PAID', paid_at = NOW()
                WHERE settlement_id = ? AND payment_status = 'UNPAID' AND manager_status = 'APPROVED'
                """, settlementId);
    }

    public int approve(Long settlementId, Long approvedBy) {
        return jdbc().update("""
                UPDATE settlement
                SET manager_status = 'APPROVED', approved_by = ?, approved_at = NOW()
                WHERE settlement_id = ? AND manager_status = 'PENDING_APPROVAL'
                """, approvedBy, settlementId);
    }

    public int reject(Long settlementId, Long approvedBy) {
        return jdbc().update("""
                UPDATE settlement
                SET manager_status = 'REJECTED', approved_by = ?, approved_at = NOW()
                WHERE settlement_id = ? AND manager_status = 'PENDING_APPROVAL'
                """, approvedBy, settlementId);
    }
}
