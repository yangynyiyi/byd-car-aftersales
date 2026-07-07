package com.byd.battery.service.impl;

import com.byd.battery.dao.BatteryHealthDao;
import com.byd.battery.model.BatteryHealthRecord;
import com.byd.battery.service.BatteryHealthService;
import com.byd.config.CacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BatteryHealthServiceImpl implements BatteryHealthService {

    private static final Logger log = LoggerFactory.getLogger(BatteryHealthServiceImpl.class);

    @Autowired
    private BatteryHealthDao batteryHealthDao;

    @Autowired
    private CacheService cacheService;

    @Override
    public String evaluateWarningLevel(BatteryHealthRecord record) {
        // DANGER: 任一指标命中即返回
        if (record.getSoh() != null
                && record.getSoh().compareTo(new BigDecimal("60")) < 0) {
            return "DANGER";
        }
        if (record.getVoltageDiff() != null
                && record.getVoltageDiff().compareTo(new BigDecimal("0.100")) > 0) {
            return "DANGER";
        }
        if (record.getMaxTemperature() != null
                && record.getMaxTemperature().compareTo(new BigDecimal("55")) > 0) {
            return "DANGER";
        }
        if (record.getMinTemperature() != null
                && record.getMinTemperature().compareTo(new BigDecimal("-10")) < 0) {
            return "DANGER";
        }
        if (record.getChargeCycles() != null && record.getChargeCycles() > 2000) {
            return "DANGER";
        }

        // WARNING: 任一指标命中（且无 DANGER）
        if (record.getSoh() != null
                && record.getSoh().compareTo(new BigDecimal("80")) < 0) {
            return "WARNING";
        }
        if (record.getVoltageDiff() != null
                && record.getVoltageDiff().compareTo(new BigDecimal("0.050")) >= 0) {
            return "WARNING";
        }
        if (record.getMaxTemperature() != null
                && record.getMaxTemperature().compareTo(new BigDecimal("45")) >= 0) {
            return "WARNING";
        }
        if (record.getMinTemperature() != null
                && record.getMinTemperature().compareTo(new BigDecimal("0")) <= 0) {
            return "WARNING";
        }
        if (record.getChargeCycles() != null && record.getChargeCycles() >= 1000) {
            return "WARNING";
        }

        return "NORMAL";
    }

    @Override
    public BatteryHealthRecord addRecord(BatteryHealthRecord record) {
        String level = evaluateWarningLevel(record);
        record.setWarningLevel(level);
        if (record.getDetectTime() == null) {
            record.setDetectTime(LocalDateTime.now());
        }
        batteryHealthDao.insert(record);
        cacheService.deleteByPrefix("battery:latest:" + record.getVin());
        cacheService.delete("battery:warnings");
        log.info("电池检测记录已保存, vin={}, SOH={}, warning={}",
                record.getVin(), record.getSoh(), level);
        return record;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BatteryHealthRecord getLatestByVin(String vin) {
        String key = "battery:latest:" + vin;
        Object cached = cacheService.get(key);
        if (cached instanceof BatteryHealthRecord) {
            return (BatteryHealthRecord) cached;
        }
        BatteryHealthRecord record = batteryHealthDao.selectLatestByVin(vin);
        if (record != null) {
            cacheService.put(key, record, 10);
        }
        return record;
    }

    @Override
    public List<BatteryHealthRecord> getAllByVin(String vin) {
        return batteryHealthDao.selectAllByVin(vin);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BatteryHealthRecord> listWarningVehicles() {
        Object cached = cacheService.get("battery:warnings");
        if (cached instanceof List) {
            return (List<BatteryHealthRecord>) cached;
        }
        List<BatteryHealthRecord> list = batteryHealthDao.selectByWarningLevels("WARNING", "DANGER");
        cacheService.put("battery:warnings", list, 5);
        return list;
    }
}
