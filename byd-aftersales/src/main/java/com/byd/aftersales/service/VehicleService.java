package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.Vehicle;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class VehicleService {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    private final VehicleDao vehicleDao;

    public VehicleService(VehicleDao vehicleDao) {
        this.vehicleDao = vehicleDao;
    }

    public void create(Vehicle vehicle) {
        validate(vehicle);
        if (vehicle.getCurrentMileage() == null) {
            vehicle.setCurrentMileage(BigDecimal.ZERO);
        }
        if (vehicle.getVehicleStatus() == null || vehicle.getVehicleStatus().isBlank()) {
            vehicle.setVehicleStatus("NORMAL");
        }
        fillDefaultReminderDates(vehicle);
        vehicleDao.insert(vehicle);
    }

    public void update(String vin, Vehicle vehicle) {
        vehicle.setVin(vin);
        Vehicle existing = findByVin(vin);
        keepExistingReminderDates(vehicle, existing);
        validate(vehicle);
        if (vehicleDao.update(vehicle) == 0) {
            throw new BusinessException("车辆不存在");
        }
    }

    private void fillDefaultReminderDates(Vehicle vehicle) {
        LocalDate purchaseDate = vehicle.getPurchaseDate() != null ? vehicle.getPurchaseDate() : LocalDate.now();
        if (vehicle.getLastMaintenanceDate() == null) {
            vehicle.setLastMaintenanceDate(purchaseDate);
        }
        if (vehicle.getNextMaintenanceDate() == null) {
            vehicle.setNextMaintenanceDate(purchaseDate.plusMonths(6));
        }
        if (vehicle.getNextInspectionDate() == null) {
            vehicle.setNextInspectionDate(purchaseDate.plusYears(2));
        }
        if (vehicle.getInsuranceExpireDate() == null) {
            vehicle.setInsuranceExpireDate(purchaseDate.plusYears(1));
        }
    }

    private void keepExistingReminderDates(Vehicle vehicle, Vehicle existing) {
        if (vehicle.getLastMaintenanceDate() == null) {
            vehicle.setLastMaintenanceDate(existing.getLastMaintenanceDate());
        }
        if (vehicle.getNextMaintenanceDate() == null) {
            vehicle.setNextMaintenanceDate(existing.getNextMaintenanceDate());
        }
        if (vehicle.getNextInspectionDate() == null) {
            vehicle.setNextInspectionDate(existing.getNextInspectionDate());
        }
        if (vehicle.getInsuranceExpireDate() == null) {
            vehicle.setInsuranceExpireDate(existing.getInsuranceExpireDate());
        }
    }

    public void delete(String vin) {
        if (vehicleDao.softDelete(vin) == 0) {
            throw new BusinessException("车辆不存在");
        }
    }

    public Vehicle findByVin(String vin) {
        return vehicleDao.findByVin(vin).orElseThrow(() -> new BusinessException("车辆不存在"));
    }

    public List<Vehicle> findByOwnerId(Long ownerId) {
        return vehicleDao.findByOwnerId(ownerId);
    }

    public List<Vehicle> findAll() {
        return vehicleDao.findAll();
    }

    private void validate(Vehicle vehicle) {
        if (vehicle.getVin() == null || !VIN_PATTERN.matcher(vehicle.getVin()).matches()) {
            throw new BusinessException("VIN 必须为 17 位，且不能包含 I、O、Q");
        }
        if (vehicle.getOwnerId() == null) {
            throw new BusinessException("车主 ID 不能为空");
        }
        if (vehicle.getModel() == null || vehicle.getModel().isBlank()) {
            throw new BusinessException("车型不能为空");
        }
        if (vehicle.getBatteryModel() == null || vehicle.getBatteryModel().isBlank()) {
            throw new BusinessException("电池型号不能为空");
        }
    }
}
