package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.SysUserDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.SysUser;
import com.byd.aftersales.domain.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class VehicleService {

    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    private static final String DEFAULT_OWNER_PASSWORD = "12345678";

    private final VehicleDao vehicleDao;
    private final SysUserDao sysUserDao;

    public VehicleService(VehicleDao vehicleDao, SysUserDao sysUserDao) {
        this.vehicleDao = vehicleDao;
        this.sysUserDao = sysUserDao;
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(Vehicle vehicle, Long operatorAdvisorId) {
        applyAdvisorScope(vehicle, operatorAdvisorId);
        resolveOwnerId(vehicle);
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

    public void update(String vin, Vehicle vehicle, Long operatorAdvisorId) {
        vehicle.setVin(vin);
        Vehicle existing = findByVin(vin);
        assertAdvisorAccess(existing, operatorAdvisorId);
        applyAdvisorScope(vehicle, operatorAdvisorId);
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

    public void delete(String vin, Long operatorAdvisorId) {
        assertAdvisorAccess(findByVin(vin), operatorAdvisorId);
        if (vehicleDao.softDelete(vin) == 0) {
            throw new BusinessException("车辆不存在");
        }
    }

    public Vehicle findByVin(String vin) {
        return vehicleDao.findByVin(vin).orElseThrow(() -> new BusinessException("车辆不存在"));
    }

    public Vehicle findByVin(String vin, Long advisorId) {
        Vehicle vehicle = findByVin(vin);
        assertAdvisorAccess(vehicle, advisorId);
        return vehicle;
    }

    public List<Vehicle> findByOwnerId(Long ownerId) {
        return vehicleDao.findByOwnerId(ownerId);
    }

    public List<Vehicle> findByAdvisorId(Long advisorId) {
        return vehicleDao.findByAdvisorId(advisorId);
    }

    public List<Vehicle> findAll() {
        return vehicleDao.findAll();
    }

    public List<Vehicle> list(Long advisorId) {
        if (advisorId != null) {
            return findByAdvisorId(advisorId);
        }
        return findAll();
    }

    private void assertAdvisorAccess(Vehicle vehicle, Long advisorId) {
        if (advisorId != null && !advisorId.equals(vehicle.getAdvisorId())) {
            throw new BusinessException("无权操作该车辆");
        }
    }

    /** 顾问操作车辆时强制绑定为本人，防止越权指定其他顾问 */
    private void applyAdvisorScope(Vehicle vehicle, Long operatorAdvisorId) {
        if (operatorAdvisorId != null) {
            vehicle.setAdvisorId(operatorAdvisorId);
        }
    }

    private void validate(Vehicle vehicle) {
        if (vehicle.getVin() == null || !VIN_PATTERN.matcher(vehicle.getVin()).matches()) {
            throw new BusinessException("VIN 必须为 17 位，且不能包含 I、O、Q");
        }
        if (vehicle.getOwnerId() == null) {
            throw new BusinessException("车主信息不完整，请填写车主姓名和手机号");
        }
        if (vehicle.getAdvisorId() == null) {
            throw new BusinessException("负责顾问不能为空");
        }
        if (vehicle.getModel() == null || vehicle.getModel().isBlank()) {
            throw new BusinessException("车型不能为空");
        }
        if (vehicle.getBatteryModel() == null || vehicle.getBatteryModel().isBlank()) {
            throw new BusinessException("电池型号不能为空");
        }
    }

    /** 新建车辆时：按手机号复用已有车主，否则自动注册车主账号 */
    private void resolveOwnerId(Vehicle vehicle) {
        if (vehicle.getOwnerId() != null) {
            return;
        }
        String ownerName = vehicle.getOwnerName() == null ? null : vehicle.getOwnerName().trim();
        String ownerPhone = vehicle.getOwnerPhone() == null ? null : vehicle.getOwnerPhone().trim();
        if (ownerName == null || ownerName.isBlank() || ownerPhone == null || ownerPhone.isBlank()) {
            throw new BusinessException("请填写车主姓名和手机号");
        }
        SysUser existing = sysUserDao.findOwnerByPhone(ownerPhone).orElse(null);
        if (existing != null) {
            vehicle.setOwnerId(existing.getUserId());
            if (ownerName != null && !ownerName.isBlank() && !ownerName.equals(existing.getRealName())) {
                existing.setRealName(ownerName);
                sysUserDao.update(existing);
            }
            return;
        }
        if (!ownerPhone.matches("^1\\d{10}$")) {
            throw new BusinessException("手机号格式不正确");
        }
        SysUser owner = new SysUser();
        owner.setUsername(generateOwnerUsername(ownerPhone));
        owner.setPassword(DEFAULT_OWNER_PASSWORD);
        owner.setRealName(ownerName);
        owner.setPhone(ownerPhone);
        owner.setRole("OWNER");
        owner.setStatus("ENABLED");
        sysUserDao.insert(owner);
        SysUser created = sysUserDao.findByUsername(owner.getUsername())
                .orElseThrow(() -> new BusinessException("车主账号创建失败"));
        vehicle.setOwnerId(created.getUserId());
    }

    private String generateOwnerUsername(String phone) {
        String base = "owner" + phone.replaceAll("\\D", "");
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }
        if (sysUserDao.findByUsername(base).isEmpty()) {
            return base;
        }
        for (int i = 1; i < 100; i++) {
            String candidate = base + i;
            if (candidate.length() > 32) {
                candidate = candidate.substring(0, 32);
            }
            if (sysUserDao.findByUsername(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BusinessException("无法生成车主用户名，请更换手机号");
    }
}
