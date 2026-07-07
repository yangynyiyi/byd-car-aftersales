package com.byd.agent.dao;

import com.byd.agent.model.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class VehicleDao extends JdbcDaoSupport {

    @Autowired
    public void setDataSourceInternal(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public Vehicle selectByVin(String vin) {
        String sql = "SELECT * FROM vehicle WHERE vin = ? AND deleted = 0";
        List<Vehicle> list = getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    Vehicle v = new Vehicle();
                    v.setVin(rs.getString("vin"));
                    v.setOwnerId(rs.getLong("owner_id"));
                    v.setLicensePlate(rs.getString("license_plate"));
                    v.setModel(rs.getString("model"));
                    v.setBatteryModel(rs.getString("battery_model"));
                    v.setPurchaseDate(rs.getDate("purchase_date") != null
                            ? rs.getDate("purchase_date").toLocalDate() : null);
                    v.setCurrentMileage(rs.getBigDecimal("current_mileage"));
                    v.setVehicleStatus(rs.getString("vehicle_status"));
                    v.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    v.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    v.setDeleted(rs.getInt("deleted"));
                    return v;
                },
                vin);
        return list.isEmpty() ? null : list.get(0);
    }
}
