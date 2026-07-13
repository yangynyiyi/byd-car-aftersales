package com.byd.aftersales.service;

import com.byd.aftersales.common.BusinessException;
import com.byd.aftersales.dao.BatteryHealthDao;
import com.byd.aftersales.dao.VehicleDao;
import com.byd.aftersales.dao.VehicleHealthDao;
import com.byd.aftersales.domain.BatteryHealthRecord;
import com.byd.aftersales.domain.Vehicle;
import com.byd.aftersales.domain.VehicleHealthItem;
import com.byd.aftersales.domain.VehicleHealthSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleHealthService {

    private final VehicleHealthDao vehicleHealthDao;
    private final VehicleDao vehicleDao;
    private final BatteryHealthDao batteryHealthDao;

    public VehicleHealthService(VehicleHealthDao vehicleHealthDao, VehicleDao vehicleDao,
                                BatteryHealthDao batteryHealthDao) {
        this.vehicleHealthDao = vehicleHealthDao;
        this.vehicleDao = vehicleDao;
        this.batteryHealthDao = batteryHealthDao;
    }

    public List<VehicleHealthSnapshot> listLatestByOwner(Long ownerId) {
        if (ownerId == null) {
            throw new BusinessException("车主 ID 不能为空");
        }
        return vehicleDao.findByOwnerId(ownerId).stream()
                .map(vehicle -> vehicleHealthDao.findLatestByVin(vehicle.getVin())
                        .map(this::attachItemsAndVehicle)
                        .orElseGet(() -> buildFallbackSnapshot(vehicle)))
                .toList();
    }

    public List<VehicleHealthSnapshot> listByVin(String vin) {
        Vehicle vehicle = vehicleDao.findByVin(vin)
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        List<VehicleHealthSnapshot> snapshots = vehicleHealthDao.findByVin(vin).stream()
                .map(this::attachItemsAndVehicle)
                .toList();
        if (snapshots.isEmpty()) {
            return List.of(buildFallbackSnapshot(vehicle));
        }
        return snapshots;
    }

    @Transactional(rollbackFor = Exception.class)
    public VehicleHealthSnapshot createSnapshot(VehicleHealthSnapshot snapshot) {
        validateSnapshot(snapshot);
        Long snapshotId = vehicleHealthDao.insertSnapshot(snapshot);
        for (VehicleHealthItem item : snapshot.getItems()) {
            validateItem(item);
            vehicleHealthDao.insertItem(snapshotId, item);
        }
        return vehicleHealthDao.findLatestByVin(snapshot.getVin())
                .map(this::attachItemsAndVehicle)
                .orElseThrow(() -> new BusinessException("车辆健康快照保存失败"));
    }

    @Transactional(rollbackFor = Exception.class)
    public void createFromBatteryRecord(BatteryHealthRecord record) {
        if (record == null || record.getVin() == null) {
            return;
        }
        Vehicle vehicle = vehicleDao.findByVin(record.getVin()).orElse(null);
        if (vehicle == null) {
            return;
        }
        createSnapshot(buildFallbackSnapshot(vehicle, record));
    }

    private VehicleHealthSnapshot attachItemsAndVehicle(VehicleHealthSnapshot snapshot) {
        snapshot.setItems(vehicleHealthDao.findItemsBySnapshotId(snapshot.getSnapshotId()));
        vehicleDao.findByVin(snapshot.getVin()).ifPresent(snapshot::setVehicle);
        return snapshot;
    }

    private VehicleHealthSnapshot buildFallbackSnapshot(Vehicle vehicle) {
        BatteryHealthRecord latest = batteryHealthDao.findLatestByVin(vehicle.getVin()).orElse(null);
        return buildFallbackSnapshot(vehicle, latest);
    }

    private VehicleHealthSnapshot buildFallbackSnapshot(Vehicle vehicle, BatteryHealthRecord latest) {
        List<VehicleHealthItem> items = buildHealthItems(vehicle, latest);
        int score = Math.max(60, 100 - items.stream()
                .mapToInt(item -> "DANGER".equals(item.getLevel()) ? 15 : "WARNING".equals(item.getLevel()) ? 8 : 0)
                .sum());
        boolean hasDanger = items.stream().anyMatch(item -> "DANGER".equals(item.getLevel()));
        boolean hasWarning = items.stream().anyMatch(item -> "WARNING".equals(item.getLevel()));
        VehicleHealthSnapshot snapshot = new VehicleHealthSnapshot();
        snapshot.setVin(vehicle.getVin());
        snapshot.setVehicle(vehicle);
        snapshot.setHealthScore(score);
        snapshot.setOverallLevel(hasDanger ? "DANGER" : hasWarning ? "WARNING" : "NORMAL");
        snapshot.setSummary(hasDanger ? "存在高风险项" : hasWarning ? "有项目建议关注" : "整体健康良好");
        snapshot.setSuggestion(hasDanger || hasWarning
                ? "建议优先处理异常/提醒项，可预约服务门店。"
                : "暂无需要立即处理的项目，建议按期保养。");
        snapshot.setDetectTime(latest != null && latest.getDetectTime() != null
                ? latest.getDetectTime() : LocalDateTime.now());
        snapshot.setItems(items);
        return snapshot;
    }

    private List<VehicleHealthItem> buildHealthItems(Vehicle vehicle, BatteryHealthRecord latest) {
        List<VehicleHealthItem> items = new ArrayList<>();
        BigDecimal voltageDiff = latest != null ? latest.getVoltageDiff() : null;
        BigDecimal maxTemperature = latest != null ? latest.getMaxTemperature() : null;
        BigDecimal soh = latest != null ? latest.getSoh() : null;
        int chargeCycles = latest != null && latest.getChargeCycles() != null ? latest.getChargeCycles() : 0;
        double mileage = vehicle.getCurrentMileage() == null ? 0 : vehicle.getCurrentMileage().doubleValue();

        String batteryLevel = latest == null ? "WARNING" : latest.getWarningLevel();
        items.add(item("BATTERY", "动力电池", batteryLevel,
                latest == null ? "暂无检测" : soh + "% SOH",
                latest == null ? "暂无电池健康检测记录，建议到店完成一次检测。" : "最近电池检测时间：" + latest.getDetectTime(),
                "NORMAL".equals(batteryLevel) ? "保持良好充电习惯" : "预约电池检测"));

        items.add(item("TIRE_PRESSURE", "轮胎/胎压", mileage >= 30000 ? "WARNING" : "NORMAL",
                String.format("%.0f km", mileage),
                mileage >= 30000 ? "里程较高，建议检查胎压、胎纹并考虑轮胎换位。" : "暂无胎压和轮胎磨损风险。",
                mileage >= 30000 ? "预约轮胎检查" : "下次保养复查"));

        items.add(item("BRAKE", "制动系统", mileage >= 40000 ? "WARNING" : "NORMAL",
                mileage >= 40000 ? "建议检查" : "正常",
                mileage >= 40000 ? "里程达到制动系统建议检查区间，关注刹车片和制动液。" : "暂无制动系统预警。",
                mileage >= 40000 ? "预约制动检查" : "保持常规检查"));

        items.add(item("LOW_VOLTAGE_BATTERY", "低压电池", chargeCycles > 500 ? "WARNING" : "NORMAL",
                chargeCycles > 500 ? "建议复检" : "正常",
                chargeCycles > 500 ? "充电循环次数较高，建议到店检查 12V 低压电池状态。" : "暂无低压电池异常。",
                chargeCycles > 500 ? "检查低压电池" : "无需处理"));

        String chargingLevel = levelByRange(voltageDiff, "0.08", "0.12");
        items.add(item("CHARGING_SYSTEM", "充电系统", chargingLevel,
                voltageDiff == null ? "暂无数据" : voltageDiff + "V 压差",
                "DANGER".equals(chargingLevel) ? "压差异常偏高，建议尽快检测充电和电池均衡状态。"
                        : "WARNING".equals(chargingLevel) ? "压差接近预警值，建议近期复检。" : "暂无充电系统异常。",
                "NORMAL".equals(chargingLevel) ? "无需处理" : "预约充电系统检测"));

        String thermalLevel = levelByRange(maxTemperature, "45", "55");
        items.add(item("THERMAL_MANAGEMENT", "热管理/高压安全", thermalLevel,
                maxTemperature == null ? "暂无数据" : maxTemperature + "°C",
                "DANGER".equals(thermalLevel) ? "温度异常偏高，需尽快检查热管理和高压安全。"
                        : "WARNING".equals(thermalLevel) ? "温度偏高，建议复检冷却系统。" : "热管理状态正常。",
                "NORMAL".equals(thermalLevel) ? "无需处理" : "预约安全检测"));

        Integer maintenanceDays = daysUntil(vehicle.getNextMaintenanceDate());
        String maintenanceLevel = dueLevel(maintenanceDays, 30, 0);
        items.add(item("MAINTENANCE", "定期保养", maintenanceLevel,
                formatDue(maintenanceDays, "日期未设置"),
                "根据车辆档案中的下次保养日期自动提醒。",
                "NORMAL".equals(maintenanceLevel) ? "按期保养" : "预约定期保养"));

        Integer inspectionDays = daysUntil(vehicle.getNextInspectionDate());
        Integer insuranceDays = daysUntil(vehicle.getInsuranceExpireDate());
        String inspectionLevel = moreSevere(dueLevel(inspectionDays, 45, 0), dueLevel(insuranceDays, 45, 0));
        items.add(item("INSPECTION_INSURANCE", "年检/保险", inspectionLevel,
                "年检：" + formatDue(inspectionDays, "未设置") + " · 保险：" + formatDue(insuranceDays, "未设置"),
                "年检和保险到期前建议提前预约门店协助检查资料。",
                "查看年检/保险提醒"));

        return items;
    }

    private VehicleHealthItem item(String type, String name, String level, String value,
                                   String description, String action) {
        VehicleHealthItem item = new VehicleHealthItem();
        item.setItemType(type);
        item.setItemName(name);
        item.setLevel(level);
        item.setMetricValue(value);
        item.setDescription(description);
        item.setActionSuggestion(action);
        return item;
    }

    private String levelByRange(BigDecimal value, String warning, String danger) {
        if (value == null) {
            return "NORMAL";
        }
        if (value.compareTo(new BigDecimal(danger)) >= 0) {
            return "DANGER";
        }
        if (value.compareTo(new BigDecimal(warning)) >= 0) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private Integer daysUntil(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    private String dueLevel(Integer days, int warningDays, int dangerDays) {
        if (days == null) {
            return "NORMAL";
        }
        if (days <= dangerDays) {
            return "DANGER";
        }
        if (days <= warningDays) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private String formatDue(Integer days, String normalText) {
        if (days == null) {
            return normalText;
        }
        if (days < 0) {
            return "已逾期 " + Math.abs(days) + " 天";
        }
        if (days == 0) {
            return "今天到期";
        }
        return days + " 天后到期";
    }

    private String moreSevere(String a, String b) {
        if ("DANGER".equals(a) || "DANGER".equals(b)) {
            return "DANGER";
        }
        if ("WARNING".equals(a) || "WARNING".equals(b)) {
            return "WARNING";
        }
        return "NORMAL";
    }

    private void validateSnapshot(VehicleHealthSnapshot snapshot) {
        if (snapshot.getVin() == null || snapshot.getVin().isBlank()) {
            throw new BusinessException("VIN 不能为空");
        }
        vehicleDao.findByVin(snapshot.getVin())
                .orElseThrow(() -> new BusinessException("车辆不存在"));
        if (snapshot.getHealthScore() == null) {
            snapshot.setHealthScore(100);
        }
        if (snapshot.getOverallLevel() == null || snapshot.getOverallLevel().isBlank()) {
            snapshot.setOverallLevel("NORMAL");
        }
        if (snapshot.getSummary() == null || snapshot.getSummary().isBlank()) {
            snapshot.setSummary("车辆健康检查");
        }
        if (snapshot.getDetectTime() == null) {
            snapshot.setDetectTime(LocalDateTime.now());
        }
    }

    private void validateItem(VehicleHealthItem item) {
        if (item.getItemType() == null || item.getItemType().isBlank()) {
            throw new BusinessException("健康项目类型不能为空");
        }
        if (item.getItemName() == null || item.getItemName().isBlank()) {
            throw new BusinessException("健康项目名称不能为空");
        }
        if (item.getLevel() == null || item.getLevel().isBlank()) {
            item.setLevel("NORMAL");
        }
    }
}
