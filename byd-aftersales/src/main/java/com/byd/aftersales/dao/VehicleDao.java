package com.byd.aftersales.dao;

import com.byd.aftersales.domain.Vehicle;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Component
public class VehicleDao extends BaseJdbcDao {

    private final RowMapper<Vehicle> rowMapper = (rs, rowNum) -> {
        Vehicle vehicle = new Vehicle();
        vehicle.setVin(rs.getString("vin"));
        vehicle.setOwnerId(rs.getLong("owner_id"));
        vehicle.setLicensePlate(rs.getString("license_plate"));
        vehicle.setModel(rs.getString("model"));
        vehicle.setBatteryModel(rs.getString("battery_model"));
        Date purchaseDate = rs.getDate("purchase_date");
        vehicle.setPurchaseDate(purchaseDate == null ? null : purchaseDate.toLocalDate());
        Date lastMaintenanceDate = rs.getDate("last_maintenance_date");
        vehicle.setLastMaintenanceDate(lastMaintenanceDate == null ? null : lastMaintenanceDate.toLocalDate());
        Date nextMaintenanceDate = rs.getDate("next_maintenance_date");
        vehicle.setNextMaintenanceDate(nextMaintenanceDate == null ? null : nextMaintenanceDate.toLocalDate());
        Date nextInspectionDate = rs.getDate("next_inspection_date");
        vehicle.setNextInspectionDate(nextInspectionDate == null ? null : nextInspectionDate.toLocalDate());
        Date insuranceExpireDate = rs.getDate("insurance_expire_date");
        vehicle.setInsuranceExpireDate(insuranceExpireDate == null ? null : insuranceExpireDate.toLocalDate());
        vehicle.setCurrentMileage(rs.getBigDecimal("current_mileage"));
        vehicle.setVehicleStatus(rs.getString("vehicle_status"));
        vehicle.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        vehicle.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        vehicle.setDeleted(rs.getInt("deleted"));
        return vehicle;
    };

    public VehicleDao(DataSource dataSource) {
        super(dataSource);
    }

    public int insert(Vehicle vehicle) {
        String sql = """
                INSERT INTO vehicle
                (vin, owner_id, license_plate, model, battery_model, purchase_date,
                 last_maintenance_date, next_maintenance_date, next_inspection_date, insurance_expire_date,
                 current_mileage, vehicle_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return jdbc().update(sql, vehicle.getVin(), vehicle.getOwnerId(), vehicle.getLicensePlate(),
                vehicle.getModel(), vehicle.getBatteryModel(), vehicle.getPurchaseDate(),
                vehicle.getLastMaintenanceDate(), vehicle.getNextMaintenanceDate(), vehicle.getNextInspectionDate(),
                vehicle.getInsuranceExpireDate(), vehicle.getCurrentMileage(), vehicle.getVehicleStatus());
    }

    public int update(Vehicle vehicle) {
        String sql = """
                UPDATE vehicle
                SET owner_id = ?, license_plate = ?, model = ?, battery_model = ?, purchase_date = ?,
                    last_maintenance_date = ?, next_maintenance_date = ?, next_inspection_date = ?,
                    insurance_expire_date = ?, current_mileage = ?, vehicle_status = ?
                WHERE vin = ? AND deleted = 0
                """;
        return jdbc().update(sql, vehicle.getOwnerId(), vehicle.getLicensePlate(), vehicle.getModel(),
                vehicle.getBatteryModel(), vehicle.getPurchaseDate(), vehicle.getLastMaintenanceDate(),
                vehicle.getNextMaintenanceDate(), vehicle.getNextInspectionDate(), vehicle.getInsuranceExpireDate(),
                vehicle.getCurrentMileage(), vehicle.getVehicleStatus(), vehicle.getVin());
    }

    public int softDelete(String vin) {
        return jdbc().update("UPDATE vehicle SET deleted = 1 WHERE vin = ?", vin);
    }

    public Optional<Vehicle> findByVin(String vin) {
        List<Vehicle> vehicles = jdbc().query("SELECT * FROM vehicle WHERE vin = ? AND deleted = 0",
                rowMapper, vin);
        return vehicles.stream().findFirst();
    }

    public List<Vehicle> findByOwnerId(Long ownerId) {
        return jdbc().query("SELECT * FROM vehicle WHERE owner_id = ? AND deleted = 0 ORDER BY created_at DESC",
                rowMapper, ownerId);
    }

    public List<Vehicle> findAll() {
        return jdbc().query("SELECT * FROM vehicle WHERE deleted = 0 ORDER BY created_at DESC", rowMapper);
    }
}
