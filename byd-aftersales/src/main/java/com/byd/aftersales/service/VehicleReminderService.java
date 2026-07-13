package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.common.IdGenerator;
import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.VehicleReminderDao;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.domain.VehicleReminder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VehicleReminderService {

    private final VehicleReminderDao reminderDao;
    private final VehicleDao vehicleDao;
    private final BatteryHealthDao batteryHealthDao;

    public VehicleReminderService(VehicleReminderDao reminderDao, VehicleDao vehicleDao,
                                  BatteryHealthDao batteryHealthDao) {
        this.reminderDao = reminderDao;
        this.vehicleDao = vehicleDao;
        this.batteryHealthDao = batteryHealthDao;
    }

    public List<VehicleReminder> listByOwner(Long ownerId) {
        if (ownerId == null) {
            throw new BusinessException("车主 ID 不能为空");
        }
        generateDueReminders(ownerId);
        return reminderDao.findByOwnerId(ownerId);
    }

    public void markRead(Long reminderId, Long ownerId) {
        if (reminderDao.markRead(reminderId, ownerId) == 0) {
            throw new BusinessException("提醒不存在");
        }
    }

    public void createBatteryReminder(String vin, Long ownerId, String level, String content) {
        createIfAbsent(vin, ownerId, "BATTERY_ALERT", level,
                "电池健康需要关注", content, LocalDateTime.now());
    }

    private void generateDueReminders(Long ownerId) {
        List<Vehicle> vehicles = vehicleDao.findByOwnerId(ownerId);
        LocalDate today = LocalDate.now();
        for (Vehicle vehicle : vehicles) {
            createDateReminder(vehicle, "MAINTENANCE_DUE", "WARNING", "定期保养即将到期",
                    "建议预约门店进行常规保养和车辆健康检查。", vehicle.getNextMaintenanceDate(), today.plusDays(30));
            createDateReminder(vehicle, "INSPECTION_DUE", "WARNING", "年检即将到期",
                    "建议提前预约年检代办，确认交强险、违章和车辆检测状态。", vehicle.getNextInspectionDate(), today.plusDays(45));
            createDateReminder(vehicle, "INSURANCE_DUE", "INFO", "保险即将到期",
                    "请关注交强险和商业险有效期，避免影响年检或上路。", vehicle.getInsuranceExpireDate(), today.plusDays(45));
            batteryHealthDao.findLatestByVin(vehicle.getVin())
                    .filter(record -> !"NORMAL".equals(record.getWarningLevel()))
                    .ifPresent(record -> createBatteryAlert(vehicle, record));
        }
    }

    private void createDateReminder(Vehicle vehicle, String type, String level, String title,
                                    String content, LocalDate date, LocalDate threshold) {
        if (date == null || date.isAfter(threshold)) {
            return;
        }
        createIfAbsent(vehicle.getVin(), vehicle.getOwnerId(), type, level, title,
                content + " 到期日：" + date, date.atStartOfDay());
    }

    private void createBatteryAlert(Vehicle vehicle, BatteryHealthRecord record) {
        String level = "DANGER".equals(record.getWarningLevel()) ? "DANGER" : "WARNING";
        String content = String.format("最新 SOH 为 %s%%，建议预约电池检测或联系服务门店。",
                record.getSoh());
        createIfAbsent(vehicle.getVin(), vehicle.getOwnerId(), "BATTERY_ALERT", level,
                "电池健康预警", content, record.getDetectTime());
    }

    private void createIfAbsent(String vin, Long ownerId, String type, String level,
                                String title, String content, LocalDateTime dueTime) {
        if (reminderDao.existsOpen(vin, type)) {
            return;
        }
        VehicleReminder reminder = new VehicleReminder();
        reminder.setReminderNo(IdGenerator.generate("RM"));
        reminder.setVin(vin);
        reminder.setOwnerId(ownerId);
        reminder.setReminderType(type);
        reminder.setLevel(level);
        reminder.setTitle(title);
        reminder.setContent(content);
        reminder.setDueTime(dueTime);
        reminder.setStatus("UNREAD");
        reminderDao.insert(reminder);
    }
}
