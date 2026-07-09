package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.OperationLogDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.domain.Vehicle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BatteryHealthService {

    private final BatteryHealthDao batteryHealthDao;
    private final VehicleDao vehicleDao;
    private final OperationLogDao operationLogDao;
    private final VehicleReminderService reminderService;
    private final VehicleHealthService vehicleHealthService;

    public BatteryHealthService(BatteryHealthDao batteryHealthDao, VehicleDao vehicleDao,
                                OperationLogDao operationLogDao,
                                VehicleReminderService reminderService,
                                VehicleHealthService vehicleHealthService) {
        this.batteryHealthDao = batteryHealthDao;
        this.vehicleDao = vehicleDao;
        this.operationLogDao = operationLogDao;
        this.reminderService = reminderService;
        this.vehicleHealthService = vehicleHealthService;
    }

    @Transactional(rollbackFor = Exception.class)
    public BatteryHealthRecord create(BatteryHealthRecord record) {
        if (record.getVin() == null || record.getVin().isBlank()) {
            throw new BusinessException("VIN 不能为空");
        }
        if (record.getSoh() == null) {
            throw new BusinessException("SOH 不能为空");
        }
        if (record.getDetectTime() == null) {
            throw new BusinessException("检测时间不能为空");
        }
        vehicleDao.findByVin(record.getVin()).orElseThrow(() -> new BusinessException("车辆不存在"));
        if (record.getWarningLevel() == null || record.getWarningLevel().isBlank()) {
            record.setWarningLevel(resolveWarningLevel(record.getSoh()));
        }
        Long id = batteryHealthDao.insert(record);
        BatteryHealthRecord saved = batteryHealthDao.findById(id)
                .orElseThrow(() -> new BusinessException("电池记录保存失败"));
        vehicleHealthService.createFromBatteryRecord(saved);
        return saved;
    }

    public List<BatteryHealthRecord> list(String level) {
        return batteryHealthDao.findByWarningLevel(level);
    }

    public List<BatteryHealthRecord> listByVin(String vin) {
        return batteryHealthDao.findByVin(vin);
    }

    public void remindOwner(String vin, Long operatorId) {
        if (vin == null || vin.isBlank()) {
            throw new BusinessException("VIN 不能为空");
        }
        if (operatorId == null) {
            throw new BusinessException("操作人不能为空");
        }
        Vehicle vehicle = vehicleDao.findByVin(vin).orElseThrow(() -> new BusinessException("车辆不存在"));
        List<BatteryHealthRecord> records = batteryHealthDao.findByVin(vin);
        if (records.isEmpty()) {
            throw new BusinessException("暂无电池检测记录");
        }
        BatteryHealthRecord latest = records.get(0);
        String detail = String.format(
                "提醒车主关注电池健康，VIN=%s，SOH=%s%%，预警=%s",
                vin, latest.getSoh(), latest.getWarningLevel());
        Long businessId = latest.getBatteryRecordId() != null ? latest.getBatteryRecordId() : 0L;
        operationLogDao.insert("BATTERY", businessId, "REMIND_OWNER", operatorId, detail);
        reminderService.createBatteryReminder(vin, vehicle.getOwnerId(), latest.getWarningLevel(), detail);
    }

    private String resolveWarningLevel(BigDecimal soh) {
        if (soh.compareTo(new BigDecimal("70")) < 0) {
            return "DANGER";
        }
        if (soh.compareTo(new BigDecimal("80")) < 0) {
            return "WARNING";
        }
        return "NORMAL";
    }
}
