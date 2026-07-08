package com.byd.aftersales.dao;

import com.byd.aftersales.domain.Part;
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
public class PartDao extends BaseJdbcDao {

    private final RowMapper<Part> rowMapper = (rs, rowNum) -> {
        Part p = new Part();
        p.setPartId(rs.getLong("part_id"));
        p.setPartNo(rs.getString("part_no"));
        p.setPartName(rs.getString("part_name"));
        p.setCategory(rs.getString("category"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setWarningThreshold(rs.getInt("warning_threshold"));
        p.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        p.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        p.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        p.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        p.setDeleted(rs.getInt("deleted"));
        return p;
    };

    public PartDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(Part part) {
        String sql = """
                INSERT INTO part
                    (part_no, part_name, category, stock_quantity, warning_threshold,
                     purchase_price, selling_price, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, part.getPartNo());
            ps.setString(2, part.getPartName());
            ps.setString(3, part.getCategory());
            ps.setInt(4, part.getStockQuantity());
            ps.setInt(5, part.getWarningThreshold());
            ps.setBigDecimal(6, part.getPurchasePrice());
            ps.setBigDecimal(7, part.getSellingPrice());
            ps.setString(8, part.getStatus() != null ? part.getStatus() : "ENABLED");
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<Part> findById(Long partId) {
        List<Part> list = jdbc().query("SELECT * FROM part WHERE part_id = ? AND deleted = 0", rowMapper, partId);
        return list.stream().findFirst();
    }

    public List<Part> findAll() {
        return jdbc().query("SELECT * FROM part WHERE deleted = 0 ORDER BY part_name", rowMapper);
    }

    public List<Part> findLowStock() {
        return jdbc().query(
                "SELECT * FROM part WHERE deleted = 0 AND stock_quantity < warning_threshold", rowMapper);
    }

    public int update(Part part) {
        return jdbc().update("""
                UPDATE part
                SET part_name = ?, category = ?, warning_threshold = ?,
                    purchase_price = ?, selling_price = ?, status = ?
                WHERE part_id = ? AND deleted = 0
                """,
                part.getPartName(), part.getCategory(), part.getWarningThreshold(),
                part.getPurchasePrice(), part.getSellingPrice(), part.getStatus(),
                part.getPartId());
    }

    public int deductStock(Long partId, int quantity) {
        return jdbc().update("""
                UPDATE part
                SET stock_quantity = stock_quantity - ?
                WHERE part_id = ? AND deleted = 0 AND stock_quantity >= ?
                """, quantity, partId, quantity);
    }

    public int addStock(Long partId, int quantity) {
        return jdbc().update(
                "UPDATE part SET stock_quantity = stock_quantity + ? WHERE part_id = ? AND deleted = 0",
                quantity, partId);
    }

    public int deleteById(Long partId) {
        return jdbc().update("UPDATE part SET deleted = 1 WHERE part_id = ?", partId);
    }

    public long countLowStock() {
        Long count = jdbc().queryForObject(
                "SELECT COUNT(*) FROM part WHERE deleted = 0 AND stock_quantity < warning_threshold", Long.class);
        return count == null ? 0 : count;
    }
}
