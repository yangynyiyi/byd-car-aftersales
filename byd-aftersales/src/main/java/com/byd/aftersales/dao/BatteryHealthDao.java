package com.byd.aftersales.dao;

import com.byd.aftersales.domain.BatteryHealthRecord;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class BatteryHealthDao extends BaseJdbcDao {

    private final RowMapper<BatteryHealthRecord> rowMapper = (rs, rowNum) -> {
        BatteryHealthRecord r = new BatteryHealthRecord();
        r.setBatteryRecordId(rs.getLong("battery_record_id"));
        r.setVin(rs.getString("vin"));
        r.setSoh(rs.getBigDecimal("soh"));
        r.setChargeCycles(rs.getInt("charge_cycles"));
        r.setMaxTemperature(rs.getBigDecimal("max_temperature"));
        r.setMinTemperature(rs.getBigDecimal("min_temperature"));
        r.setVoltageDiff(rs.getBigDecimal("voltage_diff"));
        r.setWarningLevel(rs.getString("warning_level"));
        Timestamp detectTime = rs.getTimestamp("detect_time");
        r.setDetectTime(detectTime == null ? null : detectTime.toLocalDateTime());
        Timestamp createdAt = rs.getTimestamp("created_at");
        r.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return r;
    };

    public BatteryHealthDao(DataSource dataSource) {
        super(dataSource);
    }

    public Long insert(BatteryHealthRecord record) {
        String sql = """
                INSERT INTO battery_health_record
                    (vin, soh, charge_cycles, max_temperature, min_temperature,
                     voltage_diff, warning_level, detect_time)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, record.getVin());
            ps.setBigDecimal(2, record.getSoh());
            ps.setInt(3, record.getChargeCycles() != null ? record.getChargeCycles() : 0);
            ps.setBigDecimal(4, record.getMaxTemperature());
            ps.setBigDecimal(5, record.getMinTemperature());
            ps.setBigDecimal(6, record.getVoltageDiff());
            ps.setString(7, record.getWarningLevel());
            ps.setTimestamp(8, Timestamp.valueOf(record.getDetectTime()));
            return ps;
        }, keyHolder);
        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public Optional<BatteryHealthRecord> findById(Long id) {
        List<BatteryHealthRecord> list = jdbc().query(
                "SELECT * FROM battery_health_record WHERE battery_record_id = ?", rowMapper, id);
        return list.stream().findFirst();
    }

    public List<BatteryHealthRecord> findAll() {
        return jdbc().query(
                "SELECT * FROM battery_health_record ORDER BY detect_time DESC", rowMapper);
    }

    public List<BatteryHealthRecord> findByVin(String vin) {
        return jdbc().query(
                "SELECT * FROM battery_health_record WHERE vin = ? ORDER BY detect_time DESC",
                rowMapper, vin);
    }

    public Optional<BatteryHealthRecord> findLatestByVin(String vin) {
        return findByVin(vin).stream().findFirst();
    }

    public List<BatteryHealthRecord> findByWarningLevel(String level) {
        if (level == null || level.isBlank()) {
            return findAll();
        }
        return jdbc().query(
                "SELECT * FROM battery_health_record WHERE warning_level = ? ORDER BY detect_time DESC",
                rowMapper, level);
    }

    public long countByWarningLevel(String level) {
        Long count = jdbc().queryForObject(
                "SELECT COUNT(*) FROM battery_health_record WHERE warning_level = ?", Long.class, level);
        return count == null ? 0 : count;
    }
}
