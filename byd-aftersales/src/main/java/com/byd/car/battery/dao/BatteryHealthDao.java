package com.byd.car.battery.dao;

import com.byd.car.battery.model.BatteryHealthRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class BatteryHealthDao extends JdbcDaoSupport {

    @Autowired
    public void setDataSourceInternal(DataSource dataSource) {
        setDataSource(dataSource);
    }

    public int insert(BatteryHealthRecord record) {
        String sql = "INSERT INTO battery_health_record " +
                "(vin, soh, charge_cycles, max_temperature, min_temperature, " +
                "voltage_diff, warning_level, detect_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return getJdbcTemplate().update(sql,
                record.getVin(),
                record.getSoh(),
                record.getChargeCycles(),
                record.getMaxTemperature(),
                record.getMinTemperature(),
                record.getVoltageDiff(),
                record.getWarningLevel(),
                record.getDetectTime());
    }

    public BatteryHealthRecord selectLatestByVin(String vin) {
        String sql = "SELECT * FROM battery_health_record WHERE vin = ? ORDER BY detect_time DESC LIMIT 1";
        List<BatteryHealthRecord> list = getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    BatteryHealthRecord r = new BatteryHealthRecord();
                    r.setBatteryRecordId(rs.getLong("battery_record_id"));
                    r.setVin(rs.getString("vin"));
                    r.setSoh(rs.getBigDecimal("soh"));
                    r.setChargeCycles(rs.getObject("charge_cycles") != null ? rs.getInt("charge_cycles") : null);
                    r.setMaxTemperature(rs.getBigDecimal("max_temperature"));
                    r.setMinTemperature(rs.getBigDecimal("min_temperature"));
                    r.setVoltageDiff(rs.getBigDecimal("voltage_diff"));
                    r.setWarningLevel(rs.getString("warning_level"));
                    r.setDetectTime(rs.getTimestamp("detect_time").toLocalDateTime());
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return r;
                },
                vin);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<BatteryHealthRecord> selectAllByVin(String vin) {
        String sql = "SELECT * FROM battery_health_record WHERE vin = ? ORDER BY detect_time DESC";
        return getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    BatteryHealthRecord r = new BatteryHealthRecord();
                    r.setBatteryRecordId(rs.getLong("battery_record_id"));
                    r.setVin(rs.getString("vin"));
                    r.setSoh(rs.getBigDecimal("soh"));
                    r.setChargeCycles(rs.getObject("charge_cycles") != null ? rs.getInt("charge_cycles") : null);
                    r.setMaxTemperature(rs.getBigDecimal("max_temperature"));
                    r.setMinTemperature(rs.getBigDecimal("min_temperature"));
                    r.setVoltageDiff(rs.getBigDecimal("voltage_diff"));
                    r.setWarningLevel(rs.getString("warning_level"));
                    r.setDetectTime(rs.getTimestamp("detect_time").toLocalDateTime());
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return r;
                },
                vin);
    }

    public List<BatteryHealthRecord> selectByWarningLevels(String... levels) {
        if (levels == null || levels.length == 0) {
            return List.of();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < levels.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }
        String sql = "SELECT * FROM battery_health_record " +
                "WHERE warning_level IN (" + placeholders + ") " +
                "ORDER BY detect_time DESC";
        return getJdbcTemplate().query(sql,
                (rs, rowNum) -> {
                    BatteryHealthRecord r = new BatteryHealthRecord();
                    r.setBatteryRecordId(rs.getLong("battery_record_id"));
                    r.setVin(rs.getString("vin"));
                    r.setSoh(rs.getBigDecimal("soh"));
                    r.setChargeCycles(rs.getObject("charge_cycles") != null ? rs.getInt("charge_cycles") : null);
                    r.setMaxTemperature(rs.getBigDecimal("max_temperature"));
                    r.setMinTemperature(rs.getBigDecimal("min_temperature"));
                    r.setVoltageDiff(rs.getBigDecimal("voltage_diff"));
                    r.setWarningLevel(rs.getString("warning_level"));
                    r.setDetectTime(rs.getTimestamp("detect_time").toLocalDateTime());
                    r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return r;
                },
                (Object[]) levels);
    }
}
