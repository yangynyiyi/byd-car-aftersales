package com.byd.car.part.dao;

import com.byd.car.part.entity.Part;
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
import java.util.List;
import java.util.Objects;

@Repository
public class PartDao extends JdbcDaoSupport {

    public PartDao(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Long insert(Part part) {
        String sql = """
                INSERT INTO part
                    (part_no, part_name, category, stock_quantity, warning_threshold,
                     purchase_price, selling_price, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        getJdbcTemplate().update(con -> {
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

    public Part findById(Long partId) {
        String sql = "SELECT * FROM part WHERE part_id = ? AND deleted = 0";
        List<Part> list = getJdbcTemplate().query(sql, new PartRowMapper(), partId);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Part> findAll() {
        String sql = "SELECT * FROM part WHERE deleted = 0 ORDER BY part_name";
        return getJdbcTemplate().query(sql, new PartRowMapper());
    }

    public List<Part> findLowStock() {
        String sql = "SELECT * FROM part WHERE deleted = 0 AND stock_quantity < warning_threshold";
        return getJdbcTemplate().query(sql, new PartRowMapper());
    }

    public int update(Part part) {
        String sql = """
                UPDATE part
                SET part_name = ?, category = ?, warning_threshold = ?,
                    purchase_price = ?, selling_price = ?, status = ?
                WHERE part_id = ? AND deleted = 0
                """;
        return getJdbcTemplate().update(sql,
                part.getPartName(), part.getCategory(), part.getWarningThreshold(),
                part.getPurchasePrice(), part.getSellingPrice(), part.getStatus(),
                part.getPartId());
    }

    /**
     * 扣减库存，使用乐观锁防止超卖：只在库存足够时才更新
     *
     * @return 影响行数，0 表示库存不足
     */
    public int deductStock(Long partId, int quantity) {
        String sql = """
                UPDATE part
                SET stock_quantity = stock_quantity - ?
                WHERE part_id = ? AND deleted = 0 AND stock_quantity >= ?
                """;
        return getJdbcTemplate().update(sql, quantity, partId, quantity);
    }

    public int addStock(Long partId, int quantity) {
        String sql = "UPDATE part SET stock_quantity = stock_quantity + ? WHERE part_id = ? AND deleted = 0";
        return getJdbcTemplate().update(sql, quantity, partId);
    }

    public int deleteById(Long partId) {
        String sql = "UPDATE part SET deleted = 1 WHERE part_id = ?";
        return getJdbcTemplate().update(sql, partId);
    }

    private static class PartRowMapper implements RowMapper<Part> {
        @Override
        public Part mapRow(ResultSet rs, int rowNum) throws SQLException {
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
            p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            p.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            p.setDeleted(rs.getInt("deleted"));
            return p;
        }
    }
}
