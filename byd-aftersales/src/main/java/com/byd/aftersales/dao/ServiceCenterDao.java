package com.byd.aftersales.dao;

import com.byd.aftersales.domain.ServiceCenter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Component
public class ServiceCenterDao extends BaseJdbcDao {

    private final RowMapper<ServiceCenter> rowMapper = (rs, rowNum) -> {
        ServiceCenter c = new ServiceCenter();
        c.setCenterId(rs.getLong("center_id"));
        c.setCenterName(rs.getString("center_name"));
        c.setCity(rs.getString("city"));
        c.setAddress(rs.getString("address"));
        c.setPhone(rs.getString("phone"));
        c.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        c.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        c.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
        c.setDeleted(rs.getInt("deleted"));
        return c;
    };

    public ServiceCenterDao(DataSource dataSource) {
        super(dataSource);
    }

    public List<ServiceCenter> findAll() {
        return jdbc().query("SELECT * FROM service_center WHERE deleted = 0 ORDER BY center_id", rowMapper);
    }

    public Optional<ServiceCenter> findById(Long centerId) {
        List<ServiceCenter> list = jdbc().query(
                "SELECT * FROM service_center WHERE center_id = ? AND deleted = 0", rowMapper, centerId);
        return list.stream().findFirst();
    }
}
